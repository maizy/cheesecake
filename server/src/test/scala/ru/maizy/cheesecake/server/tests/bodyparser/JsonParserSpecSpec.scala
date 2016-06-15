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
import ru.maizy.cheesecake.server.bodyparser.{ JsonParserSpec => SpecType }
import ru.maizy.cheesecake.server.tests.BaseSpec

class JsonParserSpecSpec extends BaseSpec with FlatSpecLike {

  def fromConfig(config: String): ExtractResult[SpecType] = {
    SpecType.fromConfig(ConfigFactory.parseString(config))
  }

  "JsonParserSpec" should "build from right config" in {
    // scalastyle:off
    val result = fromConfig {
      """
      {
          type: json
          json_path: "$.value[0]"
      }
      """
    }
    // scalastyle:on
    result.warnings should be(empty)
    result.result should be(defined)
    result.result.get.path.symbolic shouldBe "$.value[0]"
  }

  val badConfigs = Table(
    // scalastyle:off
    ("label", "config"),

    ("without `json_path`",
      """
      {
          type: json
      }
      """),
    ("with bad `json_path`",
      """
      {
          type: json
          json_path: "].["
      }
      """),
    ("with bad `json_path` type",
      """
      {
          type: json
          json_path: [1, 2, 3]
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
