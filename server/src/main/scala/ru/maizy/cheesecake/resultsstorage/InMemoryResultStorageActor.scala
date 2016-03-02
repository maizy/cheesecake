package ru.maizy.cheesecake.resultsstorage

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import akka.actor.{ Props, ActorLogging }
import akka.event.LoggingReceive

class InMemoryResultStorageActor extends ResultStorageActor with ActorLogging{

  override def receive: Receive = LoggingReceive(
      handlerDataMessages orElse handlerRequestMessages
    )

  def handlerDataMessages: Receive = {
    case AddEndpointCheckResults(endpointFqn, results) => ???
    case CleanEndpointCheckResults(endpointFqn) => ???
  }

  def handlerRequestMessages: Receive = {
    case GetAllEndpoints => sender() ! AllEndpoints(Seq.empty)  // FIXME
    case GetAggregatedResults(endpointsFqn, aggregates) => AggregatedResults(Map.empty)  // FIXME
  }
}

object InMemoryResultStorageActor {
  def props(): Props = Props(new InMemoryResultStorageActor)
}
