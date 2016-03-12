package ru.maizy.cheesecake.server.checker

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import java.time.ZonedDateTime
import ru.maizy.cheesecake.server.service.Endpoint

object CheckStatus extends Enumeration {
  type Type = Value
  val Ok, UnableToCheck, Unavailable = Value
}

sealed trait CheckerProtocol

abstract class AbstractCheck(endpoint: Endpoint) extends CheckerProtocol
case class Check(endpoint: Endpoint) extends AbstractCheck(endpoint)

trait CheckResult {
  def status: CheckStatus.Type
  def checkTime: ZonedDateTime
}
