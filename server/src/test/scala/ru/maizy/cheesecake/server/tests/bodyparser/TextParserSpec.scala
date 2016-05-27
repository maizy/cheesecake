package ru.maizy.cheesecake.server.tests.bodyparser

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import akka.util.ByteString
import org.scalatest.FlatSpecLike
import ru.maizy.cheesecake.server.bodyparser.TextParser
import ru.maizy.cheesecake.server.bodyparser.{ TextParserSpec => SpecType }
import ru.maizy.cheesecake.server.tests.BaseSpec

class TextParserSpec extends BaseSpec with FlatSpecLike {

  val parser = new TextParser

  "JsonParser" should "parse string value" in {
    val spec = SpecType

    val body = ByteString.fromString("text")
    parser.parse(spec, body) shouldBe Some("text")
  }
}
