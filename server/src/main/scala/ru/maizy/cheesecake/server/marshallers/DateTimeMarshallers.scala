package ru.maizy.cheesecake.server.marshallers

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import java.time.{ ZoneId, ZonedDateTime }
import java.time.format.DateTimeFormatter
import ru.maizy.cheesecake.core.utils.DateTimeUtils
import spray.json.{ JsNumber, JsObject, JsString, JsValue, RootJsonFormat }


trait DateTimeMarshallers extends JsonMarshaller {
  implicit object ZonedDateTimeFormat extends RootJsonFormat[ZonedDateTime] {
    override def write(obj: ZonedDateTime): JsValue = {
      val systemZone = ZoneId.systemDefault()
      val inSystemZone = obj.withZoneSameInstant(systemZone)
      JsObject(
        "timestamp" -> JsNumber(inSystemZone.toEpochSecond),
        "local_iso8601" -> JsString(inSystemZone.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)),
        "local_formated" -> JsString(inSystemZone.format(DateTimeUtils.humanReadableDateTimeFormat)),
        "year" -> JsNumber(inSystemZone.getYear),
        "month" -> JsNumber(inSystemZone.getMonthValue),
        "day" -> JsNumber(inSystemZone.getDayOfMonth),
        "hour" -> JsNumber(inSystemZone.getHour),
        "minute" -> JsNumber(inSystemZone.getMinute),
        "second" -> JsNumber(inSystemZone.getSecond),
        "weekday_iso8601" -> JsNumber(inSystemZone.getDayOfWeek.getValue),
        "zone" -> JsString(inSystemZone.getZone.getId)
      )
    }

    // TODO
    override def read(json: JsValue): ZonedDateTime = ???
  }

}
