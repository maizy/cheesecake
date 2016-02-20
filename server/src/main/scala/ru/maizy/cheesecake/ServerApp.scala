package ru.maizy.cheesecake

/**
  * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
  * See LICENSE.txt for details.
  */

import scala.concurrent.Future
import scala.io.StdIn
import scala.concurrent.duration._
import scala.util.{ Failure, Success }
import akka.actor.{ Props, ActorSystem }
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.util.Timeout
import ru.maizy.cheesecake.checker.{ HttpCheckResult, HttpCheck, HttpCheckerActor }
import ru.maizy.cheesecake.service.{ Service, SymbolicAddress, HttpEndpoint }


object ServerApp extends App {

  implicit val system = ActorSystem("cheesecake-server")
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
    import akka.pattern.ask
    implicit val timeout = Timeout(30.seconds)

    val endpoint1 = HttpEndpoint(SymbolicAddress("localhost"), 80, "/status")
    val endpoint2 = HttpEndpoint(SymbolicAddress("localhost"), 80, "/")
    val endpoint3 = HttpEndpoint(SymbolicAddress("localhost"), 80, "/not_found")
    val endpoint4 = HttpEndpoint(SymbolicAddress("non.exists"), 80, "/")

    val service1 = Service("nginx", Set(endpoint1, endpoint2, endpoint3, endpoint4))

    val httpChecker = system.actorOf(Props(new HttpCheckerActor(materializer)), name = "http-checker")
    val httpEndpoints = service1.endpoints.collect { case e: HttpEndpoint => e }.toSeq

    val futures = httpEndpoints.map { endpoint =>
      (httpChecker ? HttpCheck(endpoint)).mapTo[HttpCheckResult]
    } ++ httpEndpoints.map { endpoint =>
      (httpChecker ? HttpCheck(endpoint, includeResponse = true)).mapTo[HttpCheckResult]
    }

    Future.sequence(futures).onComplete {
      case Success(results) =>
        println(
          results
          .map(r => s"${r.endpoint}: Status: ${r.status} HTTP: ${r.httpStatus}} Header: ${r.headers} Body: ${r.body}")
          .mkString("\n")
        )
      case Failure(e) => println(s"Error: $e")
    }
  }
}
