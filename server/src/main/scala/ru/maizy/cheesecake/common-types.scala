package ru.maizy.cheesecake

import java.util.Calendar

/**
  * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
  * See LICENSE.txt for details.
  */

case class Timestamp(millis: Long) {
  require(millis > 0)

  def seconds(): Long = Math.round(millis.toDouble / 1000)
}

object Timestamp {
  def now(): Timestamp = Timestamp(System.currentTimeMillis)
}
