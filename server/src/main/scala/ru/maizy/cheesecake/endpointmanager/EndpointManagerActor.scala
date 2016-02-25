package ru.maizy.cheesecake.endpointmanager

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import scala.concurrent.duration.FiniteDuration
import akka.actor.{ Actor, Cancellable, ActorLogging }
import ru.maizy.cheesecake.random

abstract class EndpointManagerActor extends Actor with ActorLogging {

  implicit protected val system = context.system
  implicit protected val ec = system.dispatcher  // TODO: separate dispatcher

  private var checkTicker: Option[Cancellable] = None
  private var _checkInterval: Option[FiniteDuration] = None

  override def receive: Receive = checkTickerHandler

  protected def checkTickerHandler: Receive = {

    case SetCheckInterval(interval: FiniteDuration) =>
      disableChecking()

      // FiniteDuration.mul returns Duration because of Double.*Infinity
      val initialInterval: FiniteDuration = (interval * random.nextDouble()).asInstanceOf[FiniteDuration]
      _checkInterval = Some(interval)
      checkTicker = Some(context.system.scheduler.schedule(initialInterval, interval, self, Check))

    case DisableChecking =>
      disableChecking()

    case Check => check()
  }

  protected def disableChecking(): Unit = {
    checkTicker.foreach(_.cancel())
    checkTicker = None
  }

  protected def checkInterval: Option[FiniteDuration] = _checkInterval

  protected def check(): Unit
}
