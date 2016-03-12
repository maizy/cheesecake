package ru.maizy.cheesecake.server.resultsstorage

import ru.maizy.cheesecake.server.checker.CheckStatus
/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

object AggregateType extends Enumeration {
  type TypeKey = Value

  val UptimeDuration, UptimeChecks, LastResult = Value
}

sealed trait Aggregate {
  def key: AggregateType.TypeKey
}

case class SimpleAggregate(key: AggregateType.TypeKey) extends Aggregate

case class LastResultAggregate(status: CheckStatus.Type) extends Aggregate {
  val key = AggregateType.LastResult
}
