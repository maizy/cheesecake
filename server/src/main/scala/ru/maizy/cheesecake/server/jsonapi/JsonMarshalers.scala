package ru.maizy.cheesecake.server.jsonapi

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._
import ru.maizy.cheesecake.server.jsonapi.models.AppConfigs

trait JsonMarshalers extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val config = jsonFormat1(AppConfigs)
}
