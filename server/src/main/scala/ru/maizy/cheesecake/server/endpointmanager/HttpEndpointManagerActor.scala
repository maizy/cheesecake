package ru.maizy.cheesecake.server.endpointmanager

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import scala.concurrent.Future
import scala.concurrent.duration._
import akka.actor.{ ActorRef, Props }
import akka.event.LoggingReceive
import akka.pattern.{ ask, pipe }
import ru.maizy.cheesecake.server.bodyparser.BodyParsers
import ru.maizy.cheesecake.server.ExtraInfo
import ru.maizy.cheesecake.server.checker.{ HttpCheck, HttpCheckResult }
import ru.maizy.cheesecake.server.service.{ EndpointStatus, HttpEndpoint }


class HttpEndpointManagerActor(httpCheckerPool: ActorRef, endpoint: HttpEndpoint) extends EndpointManagerActor {

  private val parseResponse = endpoint.bodyParsers.isDefined
  override protected def check(): Unit = {
    val timeout = checkInterval.getOrElse(1.seconds)
    val checkFuture: Future[HttpCheckResult] =
      (httpCheckerPool ? HttpCheck(endpoint, includeResponse = parseResponse))(timeout).mapTo[HttpCheckResult]
    checkFuture pipeTo self
  }

  def checkResultsHandler: Receive = {
    case checkResult: HttpCheckResult =>
      context.parent ! EndpointStatus(endpoint, checkResult.copy(extraInfo = parseBody(checkResult)))
  }

  private def parseBody(checkResult: HttpCheckResult): Option[ExtraInfo] = {
    if (!parseResponse || endpoint.bodyParsers.isEmpty) {
      None
    } else {
      checkResult.body.map { body =>
        BodyParsers.parse(body, endpoint.bodyParsers.get)
      }
    }
  }

  override def receive: Receive = LoggingReceive(checkResultsHandler orElse super.receive)

}


object HttpEndpointManagerActor {
  def props(httpCheckerPool: ActorRef, endpoint: HttpEndpoint): Props =
    Props(new HttpEndpointManagerActor(httpCheckerPool, endpoint))
}
