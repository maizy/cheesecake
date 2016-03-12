package ru.maizy.cheesecake

import scala.util.Random

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */
package object server {

  type Headers = Map[String, Seq[String]]

  val random = new Random
}
