package ru.maizy.cheesecake.server.jsonapi

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import ru.maizy.cheesecake.server.jsonapi.models.AppConfigs


class JsonApi(system: ActorSystem, host: String, port: Int) extends JsonMarshalers {

  private val services: Route =
    (path("state" / "full_view") & get) {
      complete {
        "full view"
      }
    }

  private val configs: Route = (path("configs") & get) {
    complete {
      // TODO: build from app config
      AppConfigs(wsStateUrl = "/ws/state")
    }
  }
  val routes: Route = logRequestResult("cheesecake-json-api") { // TODO: how it works
    configs ~ pathPrefix("services")(services)
  }

}
