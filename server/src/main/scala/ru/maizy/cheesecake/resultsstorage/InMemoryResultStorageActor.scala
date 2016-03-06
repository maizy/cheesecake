package ru.maizy.cheesecake.resultsstorage

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import akka.actor.{ Props, ActorLogging }
import akka.event.LoggingReceive

class InMemoryResultStorageActor extends ResultStorageActor with ActorLogging {

  override def receive: Receive = LoggingReceive(
      handlerDataMessages orElse handlerRequestMessages
    )

  def handlerDataMessages: Receive = {
    case AddEndpointCheckResults(endpointFqn, results) =>  // FIXME
    case CleanEndpointCheckResults(endpointFqn) =>  // FIXME
  }

  def handlerRequestMessages: Receive = {
    case GetAllEndpoints =>
      sender() ! AllEndpoints(Seq.empty)  // FIXME

    case GetAggregatedResults(_, _) =>
      sender() ! AggregatedResults(Map.empty)  // FIXME

    case GetEndpointCheckResults(endpointsFqn, limit) =>
      sender() ! EndpointCheckResults(Map.empty)  // FIXME

  }
}

object InMemoryResultStorageActor {
  def props(): Props = Props(new InMemoryResultStorageActor)
}
