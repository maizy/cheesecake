package ru.maizy.cheesecake.resultsstorage

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

object AggregateType extends Enumeration {
  type TypeKey = Value

  val UptimeDuration, UptimeChecks, LastSuccessTimestamp, LastFailedTimestamp, LastUnavailableTimestamp = Value
}

sealed trait Aggregate {
  def key: AggregateType.TypeKey
}

case class SimpleAggregate(key: AggregateType.TypeKey) extends Aggregate
