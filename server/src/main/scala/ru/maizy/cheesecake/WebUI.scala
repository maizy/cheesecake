package ru.maizy.cheesecake

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */


import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route


class WebUI(system: ActorSystem) {

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
