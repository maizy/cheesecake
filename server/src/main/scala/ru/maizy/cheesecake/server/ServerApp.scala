package ru.maizy.cheesecake.server

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import java.util.concurrent.atomic.AtomicBoolean
import scala.util.{ Failure, Success, Try }
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.typesafe.config.{ Config, ConfigFactory }
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import ru.maizy.cheesecake.server.jsonapi.JsonApi
import sun.misc.{ Signal, SignalHandler }

object ServerApp extends App with SignalHandler {
  val STATUS_BAD_OPTIONS = 2
  val STATUS_BAD_CONFIG = 3
  val STATUS_UNABLE_TO_BIND = 10

  val SIGING = "INT"
  val SIGTERM = "TERM"

  var actorSystem: Option[ActorSystem] = None

  val terminated = new AtomicBoolean(false)
  val appLogger = Logger(LoggerFactory.getLogger(s"${BuildInfo.organization}.${BuildInfo.projectName}"))

  appLogger.info("Subscribe to SIGING, SIGTERM signals")
  Signal.handle(new Signal(SIGING), this)
  Signal.handle(new Signal(SIGTERM), this)


  OptionParser.parse(args) match {
    case None =>
      appLogger.error("Wrong app options, exiting")
      System.exit(STATUS_BAD_OPTIONS)
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

  def startUp(opts: ServerAppOptions): Unit = {
    appLogger.info("Loading configs")
    loadConfig(opts) match {

      case Failure(e) =>
        appLogger.error(s"Unable to load configs: $e")
        System.exit(STATUS_BAD_CONFIG)

      case Success(config) =>
        implicit val system = ActorSystem("cheesecake-server", config)
        actorSystem = Some(system)
        implicit val materializer = ActorMaterializer()
        implicit val ec = system.dispatcher

        appLogger.debug(s"start with settings: ${system.settings.toString}")

        val host = opts.host
        val port = opts.port

        val initializer = new Initializer(system)

        appLogger.info("Initializing APIs & web UI")
        val wsApi = new WsApi(system, materializer)
        val jsonApi = new JsonApi(system, host, port)
        val webUi = new WebUI(system)

        val routes = jsonApi.routes ~ webUi.routes ~ wsApi.routes

        appLogger.info(s"Launching a HTTP server at http://$host:$port/")
        val bindFuture = Http().bindAndHandle(routes, host, port)

        bindFuture.onFailure {
          case e: Throwable =>
            appLogger.error(s"Unable to bind HTTP server: $e")
            System.exit(STATUS_UNABLE_TO_BIND)
        }

        bindFuture.foreach { serverBinding =>
          appLogger.info("Cheesecake HTTP server started")

          appLogger.info("Initializing checking services & endpoints")
          initializer.fromConfig(config)
        }
    }
  }

  override def handle(signal: Signal): Unit = {
    appLogger.info(s"Receive signal ${signal.getName}")
    if (terminated.compareAndSet(false, true)) {
      actorSystem foreach { system =>
        if (Seq(SIGING, SIGTERM).contains(signal.getName)) {
          appLogger.debug(s"Handle signal")
          system.terminate()
        }
      }
    } else {
      appLogger.warn("Application ever terminated, skip signal")
    }
  }
}
