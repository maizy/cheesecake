package ru.maizy.cheesecake.checker

import ru.maizy.cheesecake.service.Endpoint

/**
  * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
  * See LICENSE.txt for details.
  */

case class HttpCheck(endpoint: Endpoint, includeResponse: Boolean = false)
  extends AbstractCheck(endpoint)

case class HttpCheckResult(
  endpoint: Endpoint,
  status: CheckStatus.Type,
  httpStatus: Option[Int],
  body: Option[Seq[Byte]],
  header: Option[Map[String, IndexedSeq[String]]]
) extends CheckResult

