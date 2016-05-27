package ru.maizy.cheesecake.server.marshallers

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import ru.maizy.cheesecake.core.utils.StringUtils.upperCaseToDashes
import ru.maizy.cheesecake.server.resultsstorage.{ AggregateResult, DurationResult, IntResult, OptionalDateTimeResult }
import ru.maizy.cheesecake.server.resultsstorage.{ OptionalExtraInfoResult, OptionalStatusResult }
import spray.json.{ JsNull, JsNumber, JsObject, JsString, JsValue, RootJsonFormat, pimpAny }

trait AggregateResultJsonMarshallers
  extends DateTimeMarshallers
  with JsonMarshaller
{

  implicit object AggregateResultFormat extends RootJsonFormat[AggregateResult[Any]] {

    def write(res: AggregateResult[Any]): JsValue = JsObject(
        "value" -> writeValue(res),
        "type" -> JsString(res.typeCode)
      )

    def writeValue(res: AggregateResult[Any]): JsValue = res match {

      case int: IntResult => JsNumber(int.result)

      case optDateTime: OptionalDateTimeResult =>
        optDateTime.result match {
          case Some(dateTime) => dateTime.toJson
          case None => JsNull
        }

      case durationRes: DurationResult =>
        JsObject(
          "iso8601" -> JsString(durationRes.result.toString),
          "seconds" -> JsNumber(durationRes.result.getSeconds)
          // TODO: human readable format like 2d1h30m45s
        )

      case optStatusRes: OptionalStatusResult =>
        optStatusRes.result match {
          case Some(statusType) => JsString(upperCaseToDashes(statusType.toString))
          case None => JsNull
        }

      case optExtraInfoRes: OptionalExtraInfoResult =>
        optExtraInfoRes.result match {
          case Some(extraInfo) => JsObject(extraInfo.mapValues(JsString(_)))
          case None => JsNull
        }
    }

    def read(json: JsValue): AggregateResult[Any] = ???
  }
}
