package ru.maizy.cheesecake.server.bodyparser

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import java.util.regex.PatternSyntaxException
import scala.util.{ Failure, Success, Try }
import scala.util.matching.Regex
import com.typesafe.config.Config
import ru.maizy.cheesecake.core.RichTypesafeConfig.StringImplicits
import ru.maizy.cheesecake.server.ExtractResult

case class RegexpParserSpec(pattern: Regex) extends BodyParserSpec {
  override def parserType: BodyParserType.Type = BodyParserType.Regexp
}

case object RegexpParserSpec {

  def fromConfig(parserConfig: Config): ExtractResult[RegexpParserSpec] = {
    parserConfig.optString("pattern") match {
      case None =>
        ExtractResult.singleError("`pattern` is required for regexp body parser, skipping")

      case Some(rawPattern) =>
        Try(new Regex(rawPattern)) match {
          case Success(pattern: Regex) =>
            ExtractResult.successWithoutWarnings(RegexpParserSpec(pattern))

          case Failure(e: PatternSyntaxException) =>
            ExtractResult.singleError(s"Wrong regexp pattern '$rawPattern': $e")

          case Failure(e: Throwable) =>
            ExtractResult.singleError(s"Unknown error in patter '$rawPattern' compiling: $e")
        }
    }
  }
}
