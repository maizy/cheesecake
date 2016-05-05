package ru.maizy.cheesecake.server

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import java.net.InetAddress
import scala.concurrent.ExecutionContext
import scala.util.{ Failure, Success, Try }
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.typesafe.config.{ Config, ConfigFactory }
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import ru.maizy.cheesecake.server.resultsstorage.InMemoryResultStorageActor
import ru.maizy.cheesecake.server.checker.HttpCheckerActor
import ru.maizy.cheesecake.server.jsonapi.JsonApi
import ru.maizy.cheesecake.server.service.{ AddEndpoints, Endpoint, HttpEndpoint, Service, ServiceActor }
import ru.maizy.cheesecake.server.service.{ IpAddress, SymbolicAddress }
import ru.maizy.cheesecake.server.utils.ActorUtils.escapeActorName


object ServerApp extends App {
  val STATUS_BAD_OPTIONS = 2
  val STATUS_BAD_ADDITIONAL_CONFIG = 3

  val appLogger = Logger(LoggerFactory.getLogger(s"${BuildInfo.organization}.${BuildInfo.projectName}"))

  OptionParser.parse(args) match {
    case None => {
      appLogger.error("Wrong app options, exiting")
      System.exit(STATUS_BAD_OPTIONS)
    }
    case Some(opts) => startUp(opts)
  }

  def loadConfig(opts: ServerAppOptions): Try[Config] = {
    val loadedConfig = ConfigFactory.load()
    val appConfig = ConfigFactory
      .parseString(
        s"""
          |akka.http.client.user-agent-header = cheesecake/${Version.literal}
          |akka.http.server.server-header = cheesecake/${Version.literal} (akka-http/$${akka.version})
        """.stripMargin
      )
      .resolveWith(loadedConfig)
      .withFallback(loadedConfig)

    opts.config match {
      case None => Success(appConfig)
      case Some(additionalConfigFile) =>
        appLogger.info(s"Load additional config from $additionalConfigFile")
        Try {
          ConfigFactory.parseFile(additionalConfigFile)
            .resolveWith(appConfig)
            .withFallback(appConfig)
        }
    }
  }

  // TODO: app life management
  def startUp(opts: ServerAppOptions): Unit = {
    appLogger.info("Loading configs")
    loadConfig(opts) match {

      case Failure(e) =>
        appLogger.error(s"Additional config has some errors: $e")
        System.exit(STATUS_BAD_ADDITIONAL_CONFIG)

      case Success(config) =>
        implicit val system: ActorSystem = ActorSystem("cheesecake-server", config)
        implicit val materializer = ActorMaterializer()
        implicit val ec = system.dispatcher

        val host = opts.host
        val port = opts.port

        val initializer = new Initializer(system)
        initializer.fromConfig(config)

        appLogger.info("Initializing APIs & web UI")
        val wsApi = new WsApi(system, materializer)
        val jsonApi = new JsonApi(system, host, port)
        val webUi = new WebUI(system)

        val route = jsonApi.routes ~ webUi.routes ~ wsApi.routes

        appLogger.info("Launching a HTTP server")
        Http().bindAndHandle(route, host, port)

        appLogger.info(s"Cheesecake server started at http://$host:$port/")
    }

  }

  // FIXME tmp (will be replaced by generation from config)
  def hardcodedApp()(implicit system: ActorSystem, ec: ExecutionContext, mat: ActorMaterializer): Unit = {
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
}
