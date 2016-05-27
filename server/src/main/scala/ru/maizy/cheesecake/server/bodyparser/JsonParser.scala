package ru.maizy.cheesecake.server.bodyparser

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import scala.util.Try
import akka.util.ByteString
import spray.json._  // TODO: explicit imports here and bellow
import DefaultJsonProtocol._
import spray.json.{ JsonParser => SprayJsonParser }

class JsonParser extends BodyParser {
  override type Spec = JsonParserSpec
  override def parse(spec: Spec, body: ByteString): Option[String] = {
    Try(SprayJsonParser(ParserInput.apply(body.toArray)))
      .toOption
      .flatMap { parsed =>
        spec.path.lense.tryGet[JsValue](parsed) match {
          case Left(_) => None
          case Right(results) => results.headOption
            .collect {
              case str: JsString => str.value
              case number: JsNumber => number.toString
            }
        }
      }
  }
}
