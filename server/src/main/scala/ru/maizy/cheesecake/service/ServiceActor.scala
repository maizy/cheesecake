package ru.maizy.cheesecake.service

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import scala.concurrent.duration._
import scala.collection.mutable
import akka.actor.{ Props, ActorRef, ActorLogging, Actor }
import akka.event.LoggingReceive
import akka.stream.ActorMaterializer
import ru.maizy.cheesecake.endpointmanager.{ HttpEndpointManagerActor, SetCheckInterval }
import ru.maizy.cheesecake.resultsstorage.AddEndpointCheckResults
import ru.maizy.cheesecake.utils.ActorUtils.escapeActorName
import ru.maizy.cheesecake.utils.StreamUtils.createUniqueIdIterator


class ServiceActor(
    val service: Service,
    val storage: ActorRef,
    val httpCheckerPool: ActorRef,
    implicit val materializer: ActorMaterializer)
  extends Actor
  with ActorLogging
{

  val endpoints = mutable.Map[Endpoint, Option[ActorRef]]()
  val uniqueId = createUniqueIdIterator()
  val checkIterval = 15.seconds  // TODO: from config

  def receive: Receive = LoggingReceive(
      handleEndpointManagementMessages orElse handleEndpointStatusMessages
    )

  def handleEndpointManagementMessages: Receive = {

    case AddEndpoints(newEndpoints) =>
      for (endpoint <- newEndpoints) {
        if (!endpoints.contains(endpoint)) {
          val maybeEndpointManager = createEndpointManager(endpoint)
          maybeEndpointManager match {
            case Some(manager) =>
              log.info(s"Start endpoint manager for $endpoint")
              manager ! SetCheckInterval(checkIterval)

            case None =>
              log.error(s"Unknown endpoint type ${endpoint.getClass.getName}")
          }
          endpoints(endpoint) = maybeEndpointManager
        }
      }

    case RemoveEndpoints => ???  // TODO
    case RemoveAllEndpoints => ???  // TODO
  }

  def handleEndpointStatusMessages: Receive = {
    case EndpointStatus(endpoint, res) =>
      val endpointFqn = EndpointFQN(service, endpoint)
      storage ! AddEndpointCheckResults(endpointFqn, Seq(res))
  }

  def generateEndpointActorName(endpoint: Endpoint): String = {
    val postfix = endpoint match {
      case endpoint: HttpEndpoint => s"http-${endpoint.address.hostName}:${endpoint.port}"
      case _ => endpoint.getClass.getSimpleName
    }
    escapeActorName(s"endpoint-${uniqueId.next}-$postfix")
  }

  def createEndpointManager(endpoint: Endpoint): Option[ActorRef] = endpoint match {

    case endpoint: HttpEndpoint =>
      Some(context.actorOf(
        HttpEndpointManagerActor.props(httpCheckerPool, endpoint),
        name = generateEndpointActorName(endpoint)
      ))

    case e: Endpoint =>
      None
  }
}


object ServiceActor {
  def props(service: Service, storage: ActorRef, httpChecker: ActorRef, materializer: ActorMaterializer): Props =
    Props(new ServiceActor(service, storage, httpChecker, materializer))
}
