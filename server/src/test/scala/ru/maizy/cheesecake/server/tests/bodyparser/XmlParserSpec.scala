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
import ru.maizy.cheesecake.server.bodyparser.{ XmlParser, XmlParserSpec => SpecType }
import ru.maizy.cheesecake.server.tests.BaseSpec

class XmlParserSpec extends BaseSpec with FlatSpecLike {

  def specFromConfig(config: String): ExtractResult[SpecType] = {
    SpecType.fromConfig(ConfigFactory.parseString(config))
  }

  val parser = new XmlParser

  "XmlParser" should "match relative xpath expression" in {
    val spec = specFromConfig(
      """
      {
        type: xml
        xpath: "root/val"
      }
      """
    ).result.get

    val body = ByteString.fromString(
      """<root><val>123</val></root>"""
    )
    parser.parse(spec, body) shouldBe Some("123")
  }

  it should "parse absolute xpath expression" in {
    val spec = specFromConfig(
      """
      {
        type: xml
        xpath: "//root/@value"
      }
      """
    ).result.get

    val body = ByteString.fromString(
      """<root value="abcd"/>"""
    )
    parser.parse(spec, body) shouldBe Some("abcd")
  }

  val badJson = Table(
    "body",

    "",
    """{"root": "value"}""",
    """<other/>"""
  )

  forAll(badJson) { bodyBytes =>
    it should s"return nothing for xml $bodyBytes" in {
      val spec = specFromConfig(
        """
        {
          type: xml
          xpath: "//root/value"
        }
        """
      ).result.get

    val body = ByteString.fromString(bodyBytes)
    parser.parse(spec, body) shouldBe empty
    }
  }
}
