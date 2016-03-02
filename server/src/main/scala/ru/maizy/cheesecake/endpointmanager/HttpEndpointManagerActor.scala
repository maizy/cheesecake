package ru.maizy.cheesecake.endpointmanager

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import scala.concurrent.Future
import scala.concurrent.duration._
import akka.actor.{ Props, ActorRef }
import akka.event.LoggingReceive
import akka.pattern.{ ask, pipe }
import ru.maizy.cheesecake.checker.{ HttpCheckResult, HttpCheck }
import ru.maizy.cheesecake.service.{ EndpointStatus, HttpEndpoint }


class HttpEndpointManagerActor(httpCheckerPool: ActorRef, endpoint: HttpEndpoint) extends EndpointManagerActor {

  override protected def check(): Unit = {
    val timeout = checkInterval.getOrElse(1.seconds)
    val checkFuture: Future[HttpCheckResult] =
      (httpCheckerPool ? HttpCheck(endpoint, includeResponse = true))(timeout).mapTo[HttpCheckResult]
    checkFuture pipeTo self
  }

  def checkResultsHandler: Receive = {
    case res: HttpCheckResult => context.parent ! EndpointStatus(endpoint, res)
  }

  override def receive: Receive = LoggingReceive(checkResultsHandler orElse super.receive)

}


object HttpEndpointManagerActor {
  def props(httpCheckerPool: ActorRef, endpoint: HttpEndpoint): Props =
    Props(new HttpEndpointManagerActor(httpCheckerPool, endpoint))
}
