package ru.maizy.cheesecake.server.bodyparser

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import akka.util.ByteString

class TextParser extends BodyParser {
  override type Spec = TextParserSpec.type
  override def parse(spec: Spec, body: ByteString): Option[String] =
    Some(body.utf8String)
}
