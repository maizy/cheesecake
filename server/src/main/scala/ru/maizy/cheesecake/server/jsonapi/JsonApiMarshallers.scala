package ru.maizy.cheesecake.server.jsonapi

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import ru.maizy.cheesecake.server.jsonapi.models.AppConfigs
import ru.maizy.cheesecake.server.marshallers.{ FullViewJsonMarshallers, JsonMarshaller, ServiceJsonMarshallers }


trait JsonApiMarshallers
  extends ServiceJsonMarshallers
  with FullViewJsonMarshallers
  with JsonMarshaller
{
  implicit val configFormat = jsonFormat1(AppConfigs)
}
