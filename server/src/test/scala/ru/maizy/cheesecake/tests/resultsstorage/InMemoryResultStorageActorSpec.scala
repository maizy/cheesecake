package ru.maizy.cheesecake.tests.resultsstorage

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import java.time.temporal.ChronoUnit
import java.time.{ Duration, ZonedDateTime }
import org.scalatest.FlatSpecLike
import ru.maizy.cheesecake.checker.{ CheckStatus, HttpCheckResult }
import ru.maizy.cheesecake.resultsstorage.{ AggregateType, SimpleAggregate, EndpointCheckResults }
import ru.maizy.cheesecake.resultsstorage.{ GetEndpointCheckResults, AggregatedResults, GetAggregatedResults }
import ru.maizy.cheesecake.resultsstorage.{ AllEndpoints, GetAllEndpoints, InMemoryResultStorageActor }
import ru.maizy.cheesecake.service.{ EndpointFQN, Service, SymbolicAddress, HttpEndpoint }
import ru.maizy.cheesecake.tests.ActorSystemBaseSpec


class InMemoryResultStorageActorSpec extends ActorSystemBaseSpec with FlatSpecLike {

  trait SampleHttpEndpointData {
    val localhostAddress = SymbolicAddress("localhost")
    val port = 8888
    val path = "/status"
    val endpoint = HttpEndpoint(localhostAddress, port, path)
    val service = Service("superduper")
    val endpointFqn = EndpointFQN(service, endpoint)

    val baseTime = ZonedDateTime.now()

    val successCheckResultTime =  baseTime.plus(Duration.of(10, ChronoUnit.SECONDS))
    val successCheckResult = HttpCheckResult(
      endpoint,
      CheckStatus.Ok,
      successCheckResultTime
    )

    val allAggregates = Seq(
      SimpleAggregate(AggregateType.LastFailedTimestamp),
      SimpleAggregate(AggregateType.LastSuccessTimestamp),
      SimpleAggregate(AggregateType.LastUnavailableTimestamp),
      SimpleAggregate(AggregateType.UptimeChecks),
      SimpleAggregate(AggregateType.UptimeDuration)
    )
  }


  "InMemoryResultStorage ←GetAllEndpoints" should " return no data after init" in {
    val ref = system.actorOf(InMemoryResultStorageActor.props())

    new SampleHttpEndpointData {
      ref ! GetAllEndpoints
      expectMsg(AllEndpoints(Seq.empty))
    }
  }

  "InMemoryResultStorage ←GetAggregatedResults" should "return empty results for empty requests" in {
    val ref = system.actorOf(InMemoryResultStorageActor.props())

    new SampleHttpEndpointData {
      ref ! GetAggregatedResults(Seq.empty, Seq.empty)
      expectMsg(AggregatedResults(Map.empty))

      ref ! GetAggregatedResults(Seq(endpointFqn), Seq.empty)
      expectMsg(AggregatedResults(Map.empty))

      ref ! GetAggregatedResults(Seq.empty, allAggregates)
      expectMsg(AggregatedResults(Map.empty))
    }
  }

  "InMemoryResultStorage ←GetEndpointCheckResults" should "return empty results for empty requests" in {
    val ref = system.actorOf(InMemoryResultStorageActor.props())

    new SampleHttpEndpointData {
      ref ! GetEndpointCheckResults(Seq.empty, limit = 5)
      expectMsg(EndpointCheckResults(Map.empty))

      ref ! GetEndpointCheckResults(Seq(endpointFqn), limit = 0)
      expectMsg(EndpointCheckResults(Map.empty))
    }
  }
}
