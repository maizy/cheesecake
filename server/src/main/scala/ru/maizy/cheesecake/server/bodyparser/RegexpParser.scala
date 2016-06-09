package ru.maizy.cheesecake.server.bodyparser

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import akka.util.ByteString

class RegexpParser extends BodyParser {
  override type Spec = RegexpParserSpec

  override def parse(spec: Spec, body: ByteString): Option[String] = {
    spec.pattern
      .findFirstMatchIn(body.utf8String)
      .map { result =>
        if (result.groupCount > 0) {
          result.group(1)
        } else {
          result.matched
        }
      }
  }
}
