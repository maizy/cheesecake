package ru.maizy.cheesecake.server.tests

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import akka.actor.ActorSystem
import akka.testkit.{ ImplicitSender, TestKit }
import com.typesafe.config.ConfigFactory


abstract class ActorSystemBaseSpec(system: ActorSystem)
  extends TestKit(system)
  with BaseSpec
  with ImplicitSender
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
