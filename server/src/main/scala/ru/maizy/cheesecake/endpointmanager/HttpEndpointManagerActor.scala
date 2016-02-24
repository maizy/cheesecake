package ru.maizy.cheesecake.endpointmanager

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import scala.concurrent.Future
import scala.concurrent.duration._
import akka.actor.ActorRef
import akka.event.LoggingReceive
import akka.pattern.{ ask, pipe }
import ru.maizy.cheesecake.checker.{ HttpCheckResult, HttpCheck }
import ru.maizy.cheesecake.service.HttpEndpoint


class HttpEndpointManagerActor(checkerPool: ActorRef, endpoint: HttpEndpoint) extends EndpointManagerActor {

  override protected def check(): Unit = {
    val timeout = checkInterval.getOrElse(1.seconds)
    val checkFuture: Future[HttpCheckResult] =
      (checkerPool ? HttpCheck(endpoint, includeResponse = true))(timeout).mapTo[HttpCheckResult]
    checkFuture pipeTo self
  }

  def checkResultsHandler: Receive = {
    case res: HttpCheckResult => log.debug(res.describe)
  }

  override def receive: Receive = LoggingReceive(checkResultsHandler orElse super.receive)

}
