package ru.maizy.cheesecake.server.bodyparser

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import akka.util.ByteString

trait BodyParser {
  type Spec <: BodyParserSpec
  def parse(spec: Spec, body: ByteString): Option[String]
}
