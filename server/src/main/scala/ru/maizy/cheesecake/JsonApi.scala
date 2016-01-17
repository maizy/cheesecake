package ru.maizy.cheesecake

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

case class AppConfig(wsStateUrl: String)


object JsonApi {

  val routes: Route =
    path("configs") {
      get {
        complete {
          """{"wsStateUrl": "ws://localhost:9876/ws/state"}"""  // FIXME: make a real json
        }
      }
    }
}
