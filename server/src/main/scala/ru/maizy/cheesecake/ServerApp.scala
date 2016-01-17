package ru.maizy.cheesecake

import scala.io.StdIn

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer


object ServerApp extends App {

  implicit val system = ActorSystem("cheesecake-server")
  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher

  val wsApi = new WsApi(system, materializer)
  val route = JsonApi.routes ~ WebUI.routes ~ wsApi.routes

  val bindingFuture = Http().bindAndHandle(route, "localhost", 9876)

  println(s"Server online at http://localhost:9876/\nPress RETURN to stop...")
  StdIn.readLine()
  bindingFuture
    .flatMap(_.unbind())
    .onComplete(_ ⇒ system.terminate())
}
