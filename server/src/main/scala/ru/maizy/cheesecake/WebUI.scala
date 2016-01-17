package ru.maizy.cheesecake

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route


/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */
object WebUI {

  val routes: Route =
    pathSingleSlash {
      encodeResponse {
        getFromResource("web-ui/index.html")
      }
    } ~
    pathPrefix("assets") {
      encodeResponse {
        get {
          getFromResourceDirectory("web-ui/assets")
        }
      }
    } ~
    path("ping") {
      get {
        complete {
          "pong"
        }
      }
    }
}
