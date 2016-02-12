package ru.maizy.cheesecake.checker

/**
  * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
  * See LICENSE.txt for details.
  */

import ru.maizy.cheesecake.Timestamp
import ru.maizy.cheesecake.service.Endpoint

object CheckStatus extends Enumeration {
  type Type = Value
  val Ok, UnableToCheck, Unavailable = Value
}

trait CheckerProtocol

abstract class AbstractCheck(endpoint: Endpoint) extends CheckerProtocol
case class Check(endpoint: Endpoint) extends AbstractCheck(endpoint)

trait CheckResult extends CheckerProtocol {
  def endpoint: Endpoint
  def status: CheckStatus.Type
  def checkTime: Timestamp
}
