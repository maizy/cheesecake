package ru.maizy.cheesecake.server.jsonapi

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import ru.maizy.cheesecake.server.jsonapi.models.{ AppConfigs, AppVersion }
import ru.maizy.cheesecake.server.marshallers.{ DateTimeMarshallers, FullViewJsonMarshallers, JsonMarshaller }
import ru.maizy.cheesecake.server.marshallers.ServiceJsonMarshallers


trait JsonApiMarshallers
  extends ServiceJsonMarshallers
  with FullViewJsonMarshallers
  with DateTimeMarshallers
  with JsonMarshaller
{
  implicit val configFormat = jsonFormat1(AppConfigs)
  implicit val versionFormat = jsonFormat2(AppVersion)
}
