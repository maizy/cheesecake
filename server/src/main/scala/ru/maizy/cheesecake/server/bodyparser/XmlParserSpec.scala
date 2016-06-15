package ru.maizy.cheesecake.server.bodyparser

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import javax.xml.xpath.{ XPathExpressionException, XPathFactory }
import scala.util.{ Failure, Success, Try }
import com.typesafe.config.Config
import kantan.xpath.XPathExpression
import ru.maizy.cheesecake.server.ExtractResult
import ru.maizy.cheesecake.core.RichTypesafeConfig.StringImplicits

case class XmlParserSpec(xpath: XPathExpression, symbolic: String) extends BodyParserSpec {
  override def parserType: BodyParserType.Type = BodyParserType.Xml
}

object XmlParserSpec {

  val xpathContext = XPathFactory.newInstance().newXPath()

  def fromConfig(parserConfig: Config): ExtractResult[XmlParserSpec] = {
    parserConfig.optString("xpath") match {
      case None =>
        ExtractResult.singleError("`xpath` is required for xml body parser")

      case Some(xPath) =>
        Try(xpathContext.compile(xPath)) match {
          case Success(compiled: XPathExpression) =>
            ExtractResult.successWithoutWarnings(XmlParserSpec(compiled, xPath))

          case Failure(e: XPathExpressionException) =>
            ExtractResult.singleError(s"Wrong xpath '$xPath': $e")

          case Failure(e: Throwable) =>
            ExtractResult.singleError(s"Unknown error in xpath '$xPath' compiling: $e")
        }
    }
  }
}
