package ru.maizy.cheesecake

/**
  * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
  * See LICENSE.txt for details.
  */

case class Timestamp(millis: Int) {
  require(millis > 0)

  def seconds(): Int = Math.round(millis.toDouble / 1000).toInt
}
