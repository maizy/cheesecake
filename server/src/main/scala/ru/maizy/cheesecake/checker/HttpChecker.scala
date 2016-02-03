package ru.maizy.cheesecake.checker

/**
  * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
  * See LICENSE.txt for details.
  */

import akka.actor.{ ActorRef, ActorLogging, Actor }
import ru.maizy.cheesecake.service.Endpoint


class HttpChecker extends Actor with ActorLogging {
  override def receive: Receive = {
    case Check(endpoint) =>
      check(endpoint, this.sender(), includeResponse = false)

    case HttpCheck(endpoint, includeResponse) =>
      check(endpoint, this.sender(), includeResponse)
  }

  def check(endpoint: Endpoint, sender: ActorRef, includeResponse: Boolean = false): Unit = ???
}
