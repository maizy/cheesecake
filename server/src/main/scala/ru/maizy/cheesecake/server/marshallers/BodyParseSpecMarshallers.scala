package ru.maizy.cheesecake.server.marshallers

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import ru.maizy.cheesecake.core.utils.StringUtils
import ru.maizy.cheesecake.server.bodyparser.{ BodyParserSpec, JsonParserSpec, RegexpParserSpec, XmlParserSpec }
import spray.json.{ JsNull, JsObject, JsString, JsValue, RootJsonFormat }

trait BodyParseSpecMarshallers extends JsonMarshaller {

  implicit object BodyParserSpecFormat extends RootJsonFormat[BodyParserSpec] {
    def write(spec: BodyParserSpec): JsValue = JsObject(
        "type" -> JsString(StringUtils.upperCaseToDashes(spec.parserType.toString)),
        "spec" -> describeSpec(spec)
      )

    def describeSpec(spec: BodyParserSpec): JsValue =
      spec match {
        case s: JsonParserSpec =>
          JsObject(
            "json_path" -> JsString(s.path.symbolic)
          )

        case s: XmlParserSpec =>
          JsObject(
            "xpath" -> JsString(s.symbolic)
          )

        case s: RegexpParserSpec =>
          JsObject(
            "regexp" -> JsString(s.pattern.pattern.pattern)  // :)
          )

        case _ => JsNull
      }

    override def read(json: JsValue): BodyParserSpec = ???
  }
}
