package ru.maizy.cheesecake.checker

import java.time.ZonedDateTime
import akka.util.ByteString
import ru.maizy.cheesecake.Headers
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
    checkTime: ZonedDateTime,
    httpStatus: Option[Int] = None,
    body: Option[ByteString] = None,
    headers: Option[Headers] = None
) extends CheckResult {

  def describe: String =
    s"$endpoint\n" +
    s"Status: $status HTTP: $httpStatus\n" +
    s"Headers: ${headers.getOrElse("<not parsed>")}\n" +
    s"Body: \n${body.map(" |" + _.utf8String.replace("\n", "\n |")).getOrElse("<not parsed>")}\n"

}

