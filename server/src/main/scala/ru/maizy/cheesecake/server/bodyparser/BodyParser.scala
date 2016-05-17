package ru.maizy.cheesecake.server.bodyparser

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import akka.http.scaladsl.model.HttpHeader

trait BodyParser {
  type Spec <: BodyParserSpec
  def parse(spec: Spec, body: Seq[Char], headers: Seq[HttpHeader]): String
}
