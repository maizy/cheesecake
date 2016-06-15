package ru.maizy.cheesecake.server.resultsstorage

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import java.time.{ Duration, ZonedDateTime }
import ru.maizy.cheesecake.core.utils.StringUtils
import ru.maizy.cheesecake.server.ExtraInfo
import ru.maizy.cheesecake.server.checker.CheckStatus


sealed trait AggregateResult[+T] {
  def result: T
  def typeCode: String = StringUtils.upperCaseToDashes(this.getClass.getSimpleName)
}

sealed trait OptionalAggregateResult[+T] extends AggregateResult[Option[T]]

final case class IntResult(result: Int) extends AggregateResult[Int]

final case class OptionalDateTimeResult(result: Option[ZonedDateTime])
  extends OptionalAggregateResult[ZonedDateTime]

final case class DurationResult(result: Duration)
  extends AggregateResult[Duration]

final case class OptionalStatusResult(result: Option[CheckStatus.Type])
  extends OptionalAggregateResult[CheckStatus.Type]

final case class OptionalExtraInfoResult(result: Option[ExtraInfo])
  extends OptionalAggregateResult[ExtraInfo]
