package ru.maizy.cheesecake.server.bodyparser

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import com.typesafe.config.Config
import ru.maizy.cheesecake.core.RichTypesafeConfig.StringImplicits
import ru.maizy.cheesecake.server.ExtractResult

case class JsonParserSpec(path: JsonPath) extends BodyParserSpec {
  override def parserType: BodyParserType.Type = BodyParserType.Json
}

object JsonParserSpec {
  def fromConfig(parserConfig: Config): ExtractResult[JsonParserSpec] = {
    parserConfig.optString("json_path") match {
      case None =>
        ExtractResult.singleError("`json_path` is required for json body parser, skipping")

      case Some(path) =>
        // TODO: catch json path errors
        ExtractResult.successWithoutWarnings(JsonParserSpec(JsonPath(path)))
    }
  }
}
