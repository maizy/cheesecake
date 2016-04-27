package ru.maizy.cheesecake.server.resultsstorage

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import java.time.{ Duration, ZonedDateTime }
import ru.maizy.cheesecake.core.utils.StringUtils
import ru.maizy.cheesecake.server.checker.CheckStatus


sealed trait AggregateResult[+T] {
  def result: T
  def typeCode: String = StringUtils.upperCaseToDashes(this.getClass.getSimpleName)
}

sealed trait OptionalAggregateResult[+T] extends AggregateResult[Option[T]]

case class IntResult(result: Int) extends AggregateResult[Int]

case class OptionalDateTimeResult(result: Option[ZonedDateTime])
  extends OptionalAggregateResult[ZonedDateTime]

case class DurationResult(result: Duration)
  extends AggregateResult[Duration]

case class OptionalStatusResult(result: Option[CheckStatus.Type])
  extends OptionalAggregateResult[CheckStatus.Type]
