package ru.maizy.cheesecake.server

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

case class AppConfig(wsStateUrl: String)


class JsonApi(system: ActorSystem, host: String, port: Int) {

  val routes: Route =
    path("configs") {
      get {
        complete {
          s"""{"wsStateUrl": "ws://$host:$port/ws/state"}"""  // FIXME: make a real json
        }
      }
    }
}
