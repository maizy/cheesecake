package ru.maizy.cheesecake.checker

/**
  * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
  * See LICENSE.txt for details.
  */

import akka.actor.{ ActorRef, ActorLogging, Actor }
import ru.maizy.cheesecake.Timestamp
import ru.maizy.cheesecake.service.HttpEndpoint


class HttpCheckerActor extends Actor with ActorLogging {

  override def receive: Receive = {
    case HttpCheck(endpoint, includeResponse) =>
      check(endpoint, this.sender(), includeResponse)

    case Check(endpoint: HttpEndpoint) =>
      check(endpoint, this.sender(), includeResponse = false)

    case c@Check(_) => log.error(s"Unknow check type: $c")
  }

  def check(endpoint: HttpEndpoint, sender: ActorRef, includeResponse: Boolean = false): Unit = {
    log.info(s"check $endpoint includeResponse?=$includeResponse")
    sender ! HttpCheckResult(endpoint, CheckStatus.Ok, Timestamp.now(), Some(200))
  }
}
