package ru.maizy.cheesecake.server.tests.bodyparser

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import com.typesafe.config.ConfigFactory
import org.scalatest.FlatSpecLike
import org.scalatest.prop.TableDrivenPropertyChecks._
import org.scalatest.prop.Tables.Table
import ru.maizy.cheesecake.server.ExtractResult
import ru.maizy.cheesecake.server.bodyparser.{ XmlParserSpec => SpecType }
import ru.maizy.cheesecake.server.tests.BaseSpec

class XmlParserSpecSpec extends BaseSpec with FlatSpecLike {

  def fromConfig(config: String): ExtractResult[SpecType] = {
    SpecType.fromConfig(ConfigFactory.parseString(config))
  }

  "XmlParserSpec" should "build from right config" in {
    val result = fromConfig {
      // scalastyle:off
      """
      {
          type: xml
          xpath: "//element"
      }
      """
      // scalastyle:on
    }
    result.warnings should be(empty)
    result.result should be(defined)
    result.result.get.symbolic shouldBe "//element"
  }

  val badConfigs = Table(
    // scalastyle:off
    ("label", "config"),

    ("without `xpath`",
      """
      {
          type: xml
      }
      """),
    ("with bad `xpath`",
      """
      {
          type: xml
          pattern: "//["
      }
      """),
    ("with bad `xpath` type",
      """
      {
          type: xml
          pattern: {wtf: 1}
      }
      """)
    // scalastyle:on
  )

  forAll(badConfigs) { (label, config) =>
    it should s"ignore bad config ($label) and return warnings" in {
      val result = fromConfig(config)
      result.warnings should not be empty
      result.result should be(empty)
    }
  }
}
