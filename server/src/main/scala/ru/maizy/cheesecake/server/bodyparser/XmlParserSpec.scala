package ru.maizy.cheesecake.server.bodyparser

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import com.typesafe.config.Config
import ru.maizy.cheesecake.server.ExtractResult
import ru.maizy.cheesecake.core.RichTypesafeConfig.StringImplicits

case class XmlParserSpec(xpath: String) extends BodyParserSpec {
  override def parserType: BodyParserType.Type = BodyParserType.Xml
}

object XmlParserSpec {
  def fromConfig(parserConfig: Config): ExtractResult[XmlParserSpec] = {
    parserConfig.optString("xpath") match {
      case None =>
        ExtractResult.singleError("`xpath` is required for xml body parser")

      case Some(xPath) =>
        ExtractResult.successWithoutWarnings(XmlParserSpec(xPath))
    }
  }
}
