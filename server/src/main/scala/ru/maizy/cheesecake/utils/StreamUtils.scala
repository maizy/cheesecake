package ru.maizy.cheesecake.utils

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

object StreamUtils {

  def createUniqueIdIterator(): Iterator[String] = Stream.iterate(0)(_ + 1).map(_.formatted("%03d")).iterator
}
