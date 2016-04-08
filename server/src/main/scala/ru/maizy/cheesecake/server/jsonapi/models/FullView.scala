package ru.maizy.cheesecake.server.jsonapi.models

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import ru.maizy.cheesecake.server.resultsstorage.{ Aggregate, AggregateResult }
import ru.maizy.cheesecake.server.service.{ EndpointFQN, Service }

case class FullView(resultsGrouped: Map[Service, Map[EndpointFQN, Map[Aggregate, AggregateResult[Any]]]])

object FullView {
  def groupAggregatedResults(results: Map[EndpointFQN, Map[Aggregate, AggregateResult[Any]]]): FullView = {
    FullView(results.groupBy { case (endpointFqn, _) => endpointFqn.service})
  }
}
