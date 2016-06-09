package ru.maizy.cheesecake.server.bodyparser

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import akka.util.ByteString

class XmlParser extends BodyParser {
  override type Spec = XmlParserSpec
  override def parse(spec: Spec, body: ByteString): Option[String] =
    None
}
