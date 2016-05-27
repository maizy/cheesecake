package ru.maizy.cheesecake.server.resultsstorage

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import ru.maizy.cheesecake.core.utils.StringUtils
import ru.maizy.cheesecake.server.checker.CheckStatus

object AggregateType extends Enumeration {
  type TypeKey = Value

  val UptimeDuration, UptimeChecks, LastResult, CurrentStatus, CurrentExtraInfo = Value
}

sealed trait Aggregate {
  def typeKey: AggregateType.TypeKey
  def uniqueKey: String = toString
}

case class SimpleAggregate(typeKey: AggregateType.TypeKey) extends Aggregate {
  override def uniqueKey: String = StringUtils.upperCaseToDashes(typeKey.toString)
}

case class LastResultAggregate(status: CheckStatus.Type) extends Aggregate {
  val typeKey = AggregateType.LastResult
  override def uniqueKey: String = "last-" + StringUtils.upperCaseToDashes(status.toString)
}
