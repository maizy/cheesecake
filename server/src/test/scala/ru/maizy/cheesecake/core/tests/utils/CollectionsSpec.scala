package ru.maizy.cheesecake.core.tests.utils

import org.scalatest.FlatSpecLike
import ru.maizy.cheesecake.core.utils.CollectionsUtils
import ru.maizy.cheesecake.core.tests.BaseSpec

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */
class CollectionsSpec extends BaseSpec with FlatSpecLike {

  "tuplesToMultiMap" should "work" in {
    val tuples = List(("a", 1), ("a", 2), ("b", 3))
    CollectionsUtils.tuplesToMultiMap(tuples) shouldBe Map("a" -> Seq(1, 2), "b" -> Seq(3))
  }
}
