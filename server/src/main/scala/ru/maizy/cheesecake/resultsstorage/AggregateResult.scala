package ru.maizy.cheesecake.resultsstorage

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import java.time.Duration
import ru.maizy.cheesecake.Timestamp


sealed trait AggregateResult[+T] {
  def aggregate: Aggregate
  def result: T
}

sealed trait OptionalAggregateResult[+T] extends AggregateResult[Option[T]]

case class IntResult(aggregate: Aggregate, result: Int) extends AggregateResult[Int]

case class OptionalIntResult(aggregate: Aggregate, result: Option[Int])
  extends OptionalAggregateResult[Int]

case class OptionalTimestampResult(aggregate: Aggregate, result: Option[Timestamp])
  extends OptionalAggregateResult[Timestamp]

case class DurationResult(aggregate: Aggregate, result: Duration)
  extends AggregateResult[Duration]
