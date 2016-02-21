package ru.maizy.cheesecake.checker

import akka.util.ByteString
import ru.maizy.cheesecake.{ Headers, Timestamp }
import ru.maizy.cheesecake.service.HttpEndpoint

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
  httpStatus: Option[Int] = None,
  body: Option[ByteString] = None,
  headers: Option[Headers] = None
) extends CheckResult

