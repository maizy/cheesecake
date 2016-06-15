package ru.maizy.cheesecake.server.bodyparser

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import akka.util.ByteString
import ru.maizy.cheesecake.server.ExtraInfo

object BodyParsers {
  def parse(body: ByteString, parsersSpecs: Map[String, BodyParserSpec]): ExtraInfo = {
    parsersSpecs
      .mapValues {
        case spec: JsonParserSpec =>
          (new JsonParser).parse(spec, body)

        case TextParserSpec =>
          (new TextParser).parse(TextParserSpec, body)

        case spec: RegexpParserSpec =>
          (new RegexpParser).parse(spec, body)

        case spec: XmlParserSpec =>
          (new XmlParser).parse(spec, body)

        case _ =>
          // TODO: warning
          None
      }
      .collect {
        case (key, Some(value)) => key -> value
      }
  }
}
