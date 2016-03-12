package ru.maizy.cheesecake.server.resultsstorage

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import ru.maizy.cheesecake.server.checker.CheckResult
import ru.maizy.cheesecake.server.service.{ Service, EndpointFQN }

sealed trait ResultStorageProtocol

case class AddEndpointCheckResults(endpointFqn: EndpointFQN, results: Seq[CheckResult])
  extends ResultStorageProtocol

case class ClearEndpointCheckResults(endpointFqn: EndpointFQN)


case object GetAllEndpoints extends ResultStorageProtocol

case class AllEndpoints(endpointsFqns: Seq[EndpointFQN])

case object GetAllServices extends ResultStorageProtocol

case class AllServices(services: Seq[Service])


case class GetAggregatedResults(endpointsFqns: Seq[EndpointFQN], aggregates: Seq[Aggregate])
  extends ResultStorageProtocol

case class AggregatedResults(results: Map[EndpointFQN, Map[Aggregate, AggregateResult[Any]]])


case class GetEndpointCheckResults(endpointsFqns: Seq[EndpointFQN], limit: Int = Int.MaxValue)
  extends ResultStorageProtocol
{
  require(limit >= 0)
}


// Ordered from newest to oldest
case class EndpointCheckResults(results: Map[EndpointFQN, IndexedSeq[CheckResult]])
