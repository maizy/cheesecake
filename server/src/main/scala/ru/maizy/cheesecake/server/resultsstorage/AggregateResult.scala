package ru.maizy.cheesecake.server.resultsstorage

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import java.time.{ ZonedDateTime, Duration }


sealed trait AggregateResult[+T] {
  def aggregate: Aggregate
  def result: T
}

sealed trait OptionalAggregateResult[+T] extends AggregateResult[Option[T]]

case class IntResult(aggregate: Aggregate, result: Int) extends AggregateResult[Int]

case class OptionalIntResult(aggregate: Aggregate, result: Option[Int])
  extends OptionalAggregateResult[Int]

case class OptionalDateTimeResult(aggregate: Aggregate, result: Option[ZonedDateTime])
  extends OptionalAggregateResult[ZonedDateTime]

case class DurationResult(aggregate: Aggregate, result: Duration)
  extends AggregateResult[Duration]
