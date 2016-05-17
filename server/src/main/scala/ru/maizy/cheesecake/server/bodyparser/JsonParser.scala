package ru.maizy.cheesecake.server.bodyparser

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import akka.http.scaladsl.model.HttpHeader

case class JsonPath(path: String)  // TODO: replase with some type from the json lenses lib ?

class JsonBodyParser extends BodyParser {
  override type Spec = JsonParserSpec
  // FIXME
  override def parse(spec: Spec, body: Seq[Char], headers: Seq[HttpHeader]): String = ???
}
