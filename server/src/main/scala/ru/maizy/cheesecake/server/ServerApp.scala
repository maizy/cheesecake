package ru.maizy.cheesecake.server

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import java.net.InetAddress
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
import ru.maizy.cheesecake.server.jsonapi.JsonApi
import ru.maizy.cheesecake.server.service.{ AddEndpoints, Endpoint, HttpEndpoint, Service, ServiceActor }
import ru.maizy.cheesecake.server.service.{ IpAddress, SymbolicAddress }
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
          |akka.http.server.server-header = cheesecake/${Version.literal} (akka-http/$${akka.version})
        """.stripMargin
      ).resolveWith(loadedConfig) withFallback loadedConfig

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

  // FIXME tmp (will be replaced by generation from config)
  def hardcodedApp()(implicit system: ActorSystem, ec: ExecutionContext, mat: ActorMaterializer): Unit = {
    implicit val timeout = Timeout(30.seconds)

    val storage = system.actorOf(InMemoryResultStorageActor.props(), "storage")

    val endpoint1 = HttpEndpoint(SymbolicAddress("localhost"), 80, "/status")
    val endpoint2 = HttpEndpoint(SymbolicAddress("localhost"), 80, "/")
    val endpoint3 = HttpEndpoint(
      IpAddress(InetAddress.getByAddress(Array(127, 0, 0, 1).map(_.toByte))), 80, "/not_found")
    val endpoint4 = HttpEndpoint(SymbolicAddress("non.exists"), 80, "/")

    val service1 = Service("nginx")
    val service2 = Service("my_backend")

    val endpoints1: Set[Endpoint] = Set(endpoint1, endpoint2, endpoint3)
    val endpoints2: Set[Endpoint] = Set(endpoint4)

    val httpChecker = system.actorOf(HttpCheckerActor.props(mat), name = "http-checker-1")

    val serviceActor1 = system.actorOf(
      ServiceActor.props(service1, storage, httpChecker, mat),
      name = escapeActorName(s"service-${service1.name}")
    )

    val serviceActor2 = system.actorOf(
      ServiceActor.props(service2, storage, httpChecker, mat),
      name = escapeActorName(s"service-${service2.name}")
    )

    serviceActor1 ! AddEndpoints(endpoints1)
    serviceActor2 ! AddEndpoints(endpoints2)
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
