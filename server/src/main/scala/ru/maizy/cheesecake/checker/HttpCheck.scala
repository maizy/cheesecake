package ru.maizy.cheesecake.checker

import ru.maizy.cheesecake.{ Headers, Timestamp }
import ru.maizy.cheesecake.service.{ HttpEndpoint, Endpoint }

/**
  * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
  * See LICENSE.txt for details.
  */

case class HttpCheck(
  endpoint: HttpEndpoint,
  includeResponse: Boolean = false
) extends AbstractCheck(endpoint)

case class HttpCheckResult(
  endpoint: HttpEndpoint,
  status: CheckStatus.Type,
  checkTime: Timestamp,
  httpStatus: Option[Int],
  body: Option[Seq[Byte]],
  headers: Option[Headers]
) extends CheckResult

