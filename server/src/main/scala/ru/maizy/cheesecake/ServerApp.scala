package ru.maizy.cheesecake

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import scala.io.StdIn
import scala.concurrent.duration._
import akka.actor.{ Props, ActorSystem }
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import ru.maizy.cheesecake.checker.HttpCheckerActor
import ru.maizy.cheesecake.endpointmanager.{ SetCheckInterval, HttpEndpointManagerActor }
import ru.maizy.cheesecake.service.{ Service, SymbolicAddress, HttpEndpoint }


object ServerApp extends App {

  val loadedConfig = ConfigFactory.load()
  val config = ConfigFactory
    .parseString(
      s"""
        |akka.http.client.user-agent-header = cheesecake/${Version.literal}
      """.stripMargin
    ) withFallback loadedConfig

  implicit val system = ActorSystem("cheesecake-server", config)
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

  // TODO: app life management
  println(s"Server online at http://$host:$port/\nPress RETURN to stop...")
  StdIn.readLine()
  bindingFuture
    .flatMap(_.unbind())
    .onComplete(_ => system.terminate())

  def hardcodedApp(): Unit = {
    implicit val timeout = Timeout(30.seconds)

    val endpoint1 = HttpEndpoint(SymbolicAddress("localhost"), 80, "/status")
    val endpoint2 = HttpEndpoint(SymbolicAddress("localhost"), 80, "/")
    val endpoint3 = HttpEndpoint(SymbolicAddress("localhost"), 80, "/not_found")
    val endpoint4 = HttpEndpoint(SymbolicAddress("non.exists"), 80, "/")

    val service1 = Service("nginx", Set(endpoint1, endpoint2, endpoint3, endpoint4))

    val httpChecker = system.actorOf(Props(new HttpCheckerActor(materializer)), name = "http-checker-1")
    val httpEndpoints = service1.endpoints.collect { case e: HttpEndpoint => e }.toSeq

    val number = Stream.iterate(0)(_ + 1).iterator
    for (httpEndpoint <- httpEndpoints) {
      val endpointManager = system.actorOf(
        Props(new HttpEndpointManagerActor(httpChecker, httpEndpoint)),
        name = s"endpoint-manager-${number.next}"
      )
      endpointManager ! SetCheckInterval(5.seconds)
    }
  }
}
