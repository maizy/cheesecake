package ru.maizy.cheesecake.server.jsonapi

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import ru.maizy.cheesecake.server.jsonapi.models.{ AppConfigs, FullView }
import ru.maizy.cheesecake.server.marshalers.CommonJsonMarshallers

trait JsonApiMarshallers extends CommonJsonMarshallers {
  implicit val configFormat = jsonFormat1(AppConfigs)
  implicit val fullViewFormat = jsonFormat2(FullView)
}
