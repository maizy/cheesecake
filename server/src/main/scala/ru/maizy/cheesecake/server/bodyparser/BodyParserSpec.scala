package ru.maizy.cheesecake.server.bodyparser

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */


object BodyParserType extends Enumeration {
  type Type = Value
  val Text, Regexp, Json, Xml = Value
}

trait BodyParserSpec {
  def parserType: BodyParserType.Type
}
