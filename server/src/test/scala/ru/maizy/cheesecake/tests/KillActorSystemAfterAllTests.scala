package ru.maizy.cheesecake.tests

import akka.testkit.TestKit
import org.scalatest.{ Suite, BeforeAndAfterAll }

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */
trait KillActorSystemAfterAllTests extends BeforeAndAfterAll {

  this: TestKit with Suite =>

  override protected def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }
}
