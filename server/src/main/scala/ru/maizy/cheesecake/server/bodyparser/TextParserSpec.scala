package ru.maizy.cheesecake.server.bodyparser

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */
case object TextParserSpec extends BodyParserSpec {
  override def parserType: BodyParserType.Type = BodyParserType.Text
}
