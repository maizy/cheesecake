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
import ru.maizy.cheesecake.server.bodyparser.{ RegexpParser, RegexpParserSpec => SpecType }
import ru.maizy.cheesecake.server.tests.BaseSpec

class RegexpParserSpec extends BaseSpec with FlatSpecLike {

  def specFromConfig(config: String): ExtractResult[SpecType] = {
    SpecType.fromConfig(ConfigFactory.parseString(config))
  }

  val parser = new RegexpParser

  "RegexpParser" should "match full pattern" in {
    val spec = specFromConfig(
      """
      {
        type: regexp
        pattern: "(?m)^pi: [\\d\\.]+$"
      }
      """
    ).result.get

    val body = ByteString.fromString(
      "abc\npi: 3.14"
    )
    parser.parse(spec, body) shouldBe Some("pi: 3.14")
  }

  it should "match pattern with match group" in {
    val spec = specFromConfig(
      """
      {
        type: regexp
        pattern: "(?m)^pi: ([\\d\\.]+)(.*)"
      }
      """
    ).result.get

    val body = ByteString.fromString(
      "abc\npi: 3.14abcd"
    )
    parser.parse(spec, body) shouldBe Some("3.14")
  }

  val bad = Table(
    "body",

    "not matched",
    ""
  )

  forAll(bad) { bodyBytes =>
    it should s"return nothing for body $bodyBytes" in {
      val spec = specFromConfig(
        """
        {
          type: regexp
          pattern: "(\\d+)"
        }
        """
      ).result.get

    val body = ByteString.fromString(bodyBytes)
    parser.parse(spec, body) shouldBe empty
    }
  }
}
