package ru.maizy.cheesecake.server.tests.resultsstorage

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import java.time.temporal.ChronoUnit
import java.time.{ Duration, ZonedDateTime }
import scala.collection.immutable.Queue
import akka.testkit.TestActorRef
import org.scalatest.FlatSpecLike
import ru.maizy.cheesecake.server.checker.{ CheckResult, CheckStatus, HttpCheckResult }
import ru.maizy.cheesecake.server.resultsstorage.{ ClearEndpointCheckResults, AddEndpointCheckResults, AggregateType }
import ru.maizy.cheesecake.server.resultsstorage.{ SimpleAggregate, EndpointCheckResults, GetEndpointCheckResults}
import ru.maizy.cheesecake.server.resultsstorage.{ AggregatedResults, GetAggregatedResults, AllEndpoints }
import ru.maizy.cheesecake.server.resultsstorage.{ GetAllEndpoints, InMemoryResultStorageActor }
import ru.maizy.cheesecake.server.service.{ EndpointFQN, Service, SymbolicAddress, HttpEndpoint }
import ru.maizy.cheesecake.server.tests.ActorSystemBaseSpec


class InMemoryResultStorageActorSpec extends ActorSystemBaseSpec with FlatSpecLike {

  val CHECKS_LIMIT = 10  // sometimes it will be configurable in the storage actor

  trait SampleData {
    val localhostAddress = SymbolicAddress("localhost")
    val port = 8888
    val path = "/status"
    val endpoint = HttpEndpoint(localhostAddress, port, path)
    val service = Service("superduper")
    val endpointFqn = EndpointFQN(service, endpoint)
    val otherEndpointFqn = EndpointFQN(Service("normal"), endpoint)

    val baseTime = ZonedDateTime.now()

    val successfulCheckResults: IndexedSeq[HttpCheckResult] = Range(0, CHECKS_LIMIT + 5)
      .map { i =>
        HttpCheckResult(
          CheckStatus.Ok,
          baseTime.plus(Duration.of((i + 1) * 10, ChronoUnit.MILLIS))
        )
      }

    val allAggregates = Seq(
      SimpleAggregate(AggregateType.LastFailedTimestamp),
      SimpleAggregate(AggregateType.LastSuccessTimestamp),
      SimpleAggregate(AggregateType.LastUnavailableTimestamp),
      SimpleAggregate(AggregateType.UptimeChecks),
      SimpleAggregate(AggregateType.UptimeDuration)
    )
  }

  trait WithSyncActorAndSampleData extends SampleData {
    val syncRef = TestActorRef[InMemoryResultStorageActor](InMemoryResultStorageActor.props())

    def getResults(endpointFqn: EndpointFQN): Option[InMemoryResultStorageActor#ResultData] = {
      import scala.language.existentials
      syncRef.underlyingActor.results.get(endpointFqn)
    }

    def getChecks(endpointFqn: EndpointFQN): Queue[CheckResult] = {
      val checks = getResults(endpointFqn).map(_.checks)
      checks should not be empty
      checks.get
    }

    def assertNoResults(endpointFqn: EndpointFQN): Unit = {
      getResults(endpointFqn) shouldBe empty
    }

    def assertEmptyChecks(endpointFqn: EndpointFQN): Unit = {
      val results = getResults(endpointFqn)
      results should not be empty
      results.get.checks shouldBe empty
    }
  }

  trait WithAsyncActorAndSampleData extends SampleData {
    val ref = system.actorOf(InMemoryResultStorageActor.props())
  }


  "InMemoryResultStorage ←GetAllEndpoints" should " return no data after init" in {
    new WithAsyncActorAndSampleData {
      ref ! GetAllEndpoints
      expectMsg(AllEndpoints(Seq.empty))
    }
  }

  "InMemoryResultStorage ←GetAggregatedResults" should "return empty results for empty requests" in {
    new WithAsyncActorAndSampleData {
      ref ! GetAggregatedResults(Seq.empty, Seq.empty)
      expectMsg(AggregatedResults(Map.empty))

      ref ! GetAggregatedResults(Seq(endpointFqn, otherEndpointFqn), Seq.empty)
      expectMsg(AggregatedResults(Map.empty))

      ref ! GetAggregatedResults(Seq.empty, allAggregates)
      expectMsg(AggregatedResults(Map.empty))
    }
  }

  "InMemoryResultStorage ←GetEndpointCheckResults" should "return empty results for empty requests" in {
    new WithAsyncActorAndSampleData {
      ref ! GetEndpointCheckResults(Seq.empty, limit = 5)
      expectMsg(EndpointCheckResults(Map.empty))

      ref ! GetEndpointCheckResults(Seq(endpointFqn, otherEndpointFqn), limit = 0)
      expectMsg(EndpointCheckResults(Map(endpointFqn -> Seq(), otherEndpointFqn -> Seq())))
    }
  }

  "InMemoryResultStorage ←AddEndpointCheckResults" should "add new results if them ordered by time" in {
    new WithSyncActorAndSampleData {
      successfulCheckResults.foreach {
        r => syncRef ! AddEndpointCheckResults(endpointFqn, Seq(r))
      }
      getChecks(endpointFqn) shouldBe successfulCheckResults.takeRight(CHECKS_LIMIT)
    }
  }

  it should "add new results if them send in one seq" in {
    new WithSyncActorAndSampleData {
      syncRef ! AddEndpointCheckResults(endpointFqn, successfulCheckResults)
      getChecks(endpointFqn) shouldBe successfulCheckResults.takeRight(CHECKS_LIMIT)
    }
  }

  it should "ignore old checks" in { // TODO: remove test when reordering implemented
    new WithSyncActorAndSampleData {
      syncRef ! AddEndpointCheckResults(endpointFqn, successfulCheckResults.slice(3, 6))
      syncRef ! AddEndpointCheckResults(endpointFqn, successfulCheckResults.take(3))
      getChecks(endpointFqn) shouldBe successfulCheckResults.slice(3, 6)
    }
  }

  "InMemoryResultStorage ←ClearEndpointCheckResults" should "works" in {
    new WithSyncActorAndSampleData {
      assertNoResults(endpointFqn)
      assertNoResults(otherEndpointFqn)
      syncRef ! AddEndpointCheckResults(endpointFqn, successfulCheckResults.take(3))
      getChecks(endpointFqn) shouldBe successfulCheckResults.take(3)
      syncRef ! ClearEndpointCheckResults(endpointFqn)
      assertEmptyChecks(endpointFqn)
      syncRef ! ClearEndpointCheckResults(otherEndpointFqn)
      assertNoResults(otherEndpointFqn)
    }
  }

}
