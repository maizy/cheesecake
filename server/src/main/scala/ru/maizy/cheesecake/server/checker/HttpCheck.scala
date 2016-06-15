package ru.maizy.cheesecake.server.checker

import java.time.ZonedDateTime
import akka.util.ByteString
import ru.maizy.cheesecake.server.{ Headers, ExtraInfo }
import ru.maizy.cheesecake.server.service.HttpEndpoint

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

case class HttpCheck(
    endpoint: HttpEndpoint,
    includeResponse: Boolean = false
) extends AbstractCheck(endpoint)

case class HttpCheckResult(
    status: CheckStatus.Type,
    checkTime: ZonedDateTime,
    httpStatus: Option[Int] = None,
    body: Option[ByteString] = None,
    headers: Option[Headers] = None,
    extraInfo: Option[ExtraInfo] = None
) extends CheckResult {

  def describe: String =
    s"Status: $status HTTP: $httpStatus\n" +
    s"Headers: ${headers.getOrElse("<not parsed>")}\n" +
    s"Body: \n${body.map(" |" + _.utf8String.replace("\n", "\n |")).getOrElse("<not parsed>")}\n"

}

