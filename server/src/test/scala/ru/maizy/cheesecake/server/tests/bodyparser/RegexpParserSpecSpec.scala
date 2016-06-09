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
import ru.maizy.cheesecake.server.bodyparser.{ RegexpParserSpec => SpecType }
import ru.maizy.cheesecake.server.tests.BaseSpec

class RegexpParserSpecSpec extends BaseSpec with FlatSpecLike {

  def fromConfig(config: String): ExtractResult[SpecType] = {
    SpecType.fromConfig(ConfigFactory.parseString(config))
  }

  "RegexpParserSpec" should "build from right config" in {
    val result = fromConfig {
      // TODO: scala style has incorrect detection in multiline strings
      // scalastyle:off
      """
      {
          type: regexp
          pattern: "(?m)^pi: [\\d\\.]+$"
      }
      """
      // scalastyle:on
    }
    result.warnings should be(empty)
    result.result should be(defined)
    result.result.get.pattern.pattern.pattern shouldBe "(?m)^pi: [\\d\\.]+$"
  }

  val badConfigs = Table(
    // scalastyle:off
    ("label", "config"),

    ("without `pattern`",
      """
      {
          type: regexp
      }
      """),
    ("with bad `pattern`",
      """
      {
          type: regexp
          pattern: "\\d["
      }
      """),
    ("with bad `pattern` type",
      """
      {
          type: regexp
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
