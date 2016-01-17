package ru.maizy.cheesecake

import scala.concurrent.ExecutionContextExecutor
import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.ws.Message
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow

/**
  * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
  * See LICENSE.txt for details.
  */
class WsApi(system: ActorSystem, materializer: ActorMaterializer) {

  implicit val ec: ExecutionContextExecutor = system.dispatcher

  val routes: Route = pathPrefix("ws") {
    pathPrefix("state") {
      get {
        handleWebsocketMessages(stubFlow())
      }
    }
  }

  def stubFlow(): Flow[Message, Message, Unit] = Flow[Message]  // TODO

}
