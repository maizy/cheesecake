package ru.maizy.cheesecake.server.tests.bodyparser

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import akka.util.ByteString
import com.typesafe.config.ConfigFactory
import org.scalatest.FlatSpecLike
import org.scalatest.prop.TableDrivenPropertyChecks._
import org.scalatest.prop.Tables.Table
import ru.maizy.cheesecake.server.ExtractResult
import ru.maizy.cheesecake.server.bodyparser.JsonParser
import ru.maizy.cheesecake.server.bodyparser.{ JsonParserSpec => SpecType }
import ru.maizy.cheesecake.server.tests.BaseSpec

class JsonParserSpec extends BaseSpec with FlatSpecLike {

  def specFromConfig(config: String): ExtractResult[SpecType] = {
    SpecType.fromConfig(ConfigFactory.parseString(config))
  }

  val parser = new JsonParser

  "JsonParser" should "parse string value" in {
    val spec = specFromConfig(
      """
      {
        type: json
        json_path: "$.first.second.val[0]"
      }
      """
    ).result.get

    val body = ByteString.fromString(
      """{"first": {"second": {"val": ["a", "b"]}}}"""
    )
    parser.parse(spec, body) shouldBe Some("a")
  }

  it should "parse number value" in {
    val spec = specFromConfig(
      """
      {
        type: json
        json_path: "$.first.second.number[1]"
      }
      """
    ).result.get

    val body = ByteString.fromString(
      """{"first": {"second": {"number": [2, 3]}}}"""
    )
    parser.parse(spec, body) shouldBe Some("3")
  }

  val badJson = Table(
    "body",

    "",
    "<xml/>",
    """{value:1}""",
    """{"other":"1"}"""
  )

  forAll(badJson) { bodyBytes =>
    it should s"return nothing for json $bodyBytes" in {
      val spec = specFromConfig(
        """
        {
          type: json
          json_path: "$.value"
        }
        """
      ).result.get

    val body = ByteString.fromString(bodyBytes)
    parser.parse(spec, body) shouldBe empty
    }
  }
}
