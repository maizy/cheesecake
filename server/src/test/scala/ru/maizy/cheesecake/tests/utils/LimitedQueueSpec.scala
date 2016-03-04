package ru.maizy.cheesecake.tests.utils

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import scala.collection.immutable.Queue
import org.scalatest.FlatSpecLike
import ru.maizy.cheesecake.tests.BaseSpec
import ru.maizy.cheesecake.utils.LimitedQueue._

class LimitedQueueSpec extends BaseSpec with FlatSpecLike {

  val q = Queue[String]("A", "B", "C", "D")

  "LimitedQueue" should "have limit" in {
    q.enqueueWithLimit("E", 3) shouldBe Queue("C", "D", "E")
  }

  it should "fail in negative limit" in {
    intercept[IllegalArgumentException] {
      q.enqueueWithLimit("EE", -1)
    }
  }

  it should "return emply queue if limit eq 0" in {
    q.enqueueWithLimit("AA", 0) shouldBe Queue()
  }

  it should "return queue as is if limit not reached" in {
    q.enqueueWithLimit("E", 10) shouldBe Queue("A", "B", "C", "D", "E")
  }

}
