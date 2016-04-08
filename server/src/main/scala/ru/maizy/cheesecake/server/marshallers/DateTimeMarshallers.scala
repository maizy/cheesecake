package ru.maizy.cheesecake.server.marshallers

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import ru.maizy.cheesecake.core.utils.DateTimeUtils
import spray.json.{ JsNumber, JsObject, JsString, JsValue, RootJsonFormat }


trait DateTimeMarshallers extends JsonMarshaller {
  implicit object ZonedDateTimeFormat extends RootJsonFormat[ZonedDateTime] {
    override def write(obj: ZonedDateTime): JsValue =
      JsObject(
        "timestamp" -> JsNumber(obj.toEpochSecond),
        "local_iso8601" -> JsString(obj.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)),
        "formated" -> JsString(obj.format(DateTimeUtils.humanReadableDateTimeFormat)),
        "year" -> JsNumber(obj.getYear),
        "month" -> JsNumber(obj.getMonthValue),
        "day" -> JsNumber(obj.getDayOfMonth),
        "hour" -> JsNumber(obj.getHour),
        "minute" -> JsNumber(obj.getMinute),
        "second" -> JsNumber(obj.getSecond),
        "weekday_iso8601" -> JsNumber(obj.getDayOfWeek.getValue)
      )

    // TODO
    override def read(json: JsValue): ZonedDateTime = ???
  }

}
