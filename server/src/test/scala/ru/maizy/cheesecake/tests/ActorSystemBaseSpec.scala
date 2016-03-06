package ru.maizy.cheesecake.tests

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import akka.actor.ActorSystem
import akka.testkit.{ ImplicitSender, TestKit }
import com.typesafe.config.ConfigFactory
import org.scalatest.{ Matchers, Suite }


abstract class ActorSystemBaseSpec(system: ActorSystem)
  extends TestKit(system)
  with Suite
  with ImplicitSender
  with Matchers
  with KillActorSystemAfterAllTests
{

  def this() = this(ActorSystem(
      "TestCase",
      ConfigFactory.parseString(ActorSystemBaseSpec.akkaConfig)
    ))
}


object ActorSystemBaseSpec {
  def akkaConfig: String =
    """
    akka {
      loggers = ["akka.testkit.TestEventListener"]
      loglevel = INFO
      actor.debug = {
        lifecycle = on
        receive = on
      }
    }
    """
}
