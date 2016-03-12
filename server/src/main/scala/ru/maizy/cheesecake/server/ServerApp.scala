package ru.maizy.cheesecake.server

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }
import scala.io.StdIn
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import ru.maizy.cheesecake.server.resultsstorage.{ Aggregate, AggregateType, AggregatedResults, AllEndpoints }
import ru.maizy.cheesecake.server.resultsstorage.{ EndpointCheckResults, GetAggregatedResults, GetAllEndpoints }
import ru.maizy.cheesecake.server.resultsstorage.{ GetEndpointCheckResults, InMemoryResultStorageActor }
import ru.maizy.cheesecake.server.resultsstorage.{ LastResultAggregate, SimpleAggregate }
import ru.maizy.cheesecake.server.checker.{ CheckStatus, HttpCheckerActor }
import ru.maizy.cheesecake.server.service.{ AddEndpoints, Endpoint, HttpEndpoint, Service, ServiceActor }
import ru.maizy.cheesecake.server.service.SymbolicAddress
import ru.maizy.cheesecake.server.utils.ActorUtils.escapeActorName


object ServerApp extends App {

  startUp()

  // TODO: app life management
  def startUp(): Unit = {
    val loadedConfig = ConfigFactory.load()
    val config = ConfigFactory
      .parseString(
        s"""
          |akka.http.client.user-agent-header = cheesecake/${Version.literal}
        """.stripMargin
      ) withFallback loadedConfig

    implicit val system: ActorSystem = ActorSystem("cheesecake-server", config)
    implicit val materializer = ActorMaterializer()
    implicit val ec = system.dispatcher

    // TODO: args parsing
    val (host, port) = args.toList match {
      case p :: Nil => ("localhost", p.toInt)
      case h :: p :: Nil => (h, p.toInt)
      case _ => ("localhost", 52022)
    }

    val wsApi = new WsApi(system, materializer)
    val jsonApi = new JsonApi(system, host, port)
    val webUi = new WebUI(system)

    val route = jsonApi.routes ~ webUi.routes ~ wsApi.routes

    val bindingFuture = Http().bindAndHandle(route, host, port)

    hardcodedApp()

    system.log.info(s"Server online at http://$host:$port/")
    waitForTerminate(bindingFuture)
  }

  // FIXME tmp
  def hardcodedApp()(implicit system: ActorSystem, ec: ExecutionContext, mat: ActorMaterializer): Unit = {
    implicit val timeout = Timeout(30.seconds)

    val storage = system.actorOf(InMemoryResultStorageActor.props(), "storage")

    val endpoint1 = HttpEndpoint(SymbolicAddress("localhost"), 80, "/status")
    val endpoint2 = HttpEndpoint(SymbolicAddress("localhost"), 80, "/")
    val endpoint3 = HttpEndpoint(SymbolicAddress("localhost"), 80, "/not_found")
    val endpoint4 = HttpEndpoint(SymbolicAddress("non.exists"), 80, "/")

    val service1 = Service("nginx")

    val endpoints: Set[Endpoint] = Set(endpoint1, endpoint2, endpoint3, endpoint4)

    val httpChecker = system.actorOf(HttpCheckerActor.props(mat), name = "http-checker-1")

    val serviceActor = system.actorOf(
      ServiceActor.props(service1, storage, httpChecker, mat),
      name = escapeActorName(s"service-${service1.name}")
    )

    val allAggregates: Seq[Aggregate] = Seq(
      LastResultAggregate(CheckStatus.Unavailable),
      LastResultAggregate(CheckStatus.Ok),
      LastResultAggregate(CheckStatus.UnableToCheck),
      SimpleAggregate(AggregateType.UptimeChecks),
      SimpleAggregate(AggregateType.UptimeDuration)
    )

    serviceActor ! AddEndpoints(endpoints)

    system.scheduler.schedule(10.seconds, 10.seconds) {
      (storage ? GetAllEndpoints)
        .mapTo[AllEndpoints]
        .foreach {
          endpoints => println(s"All endpoints: $endpoints")
          val aggregateFuture = (storage ? GetAggregatedResults(endpoints.endpointsFqns, allAggregates))
            .mapTo[AggregatedResults]

          val checksFuture = (storage ? GetEndpointCheckResults(endpoints.endpointsFqns, limit = 10))
            .mapTo[EndpointCheckResults]

          for (
            aggregatedRes <- aggregateFuture;
            checks <- checksFuture
          ) {
            println(s"Checks: $checks")
            println(s"AggregatedResults: $aggregatedRes")
          }
        }
      }
    }

  // FIXME tmp
  def waitForTerminate(
      bindingFuture: Future[Http.ServerBinding])(implicit system: ActorSystem, ec: ExecutionContext): Unit = {

    StdIn.readLine()
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }
}
