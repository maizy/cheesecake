package ru.maizy.cheesecake.core.utils

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

object CollectionsUtils {
  /**
   * Convert seq of tuples to MultiMap
   * ex:
   * val tuples = List(("a", "1"), ("a", "2"), ("b", "3"))
   * tuplesToMultiMap(tuples) == Map("a" -> Seq("1", "2"), "b" -> Seq("3"))
   */
  def tuplesToMultiMap[A, B](tuples: Seq[(A, B)]): Map[A, Seq[B]] = {
    tuples
      .groupBy(_._1)
      .mapValues(_.map { case (_, v) => v })
  }
}
