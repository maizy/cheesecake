package ru.maizy.cheesecake.server.tests.resultsstorage

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import java.time.temporal.ChronoUnit
import java.time.{ Duration, ZonedDateTime }
import scala.collection.immutable.Queue
import scala.concurrent.duration.DurationInt
import akka.testkit.TestActorRef
import org.scalatest.FlatSpecLike
import ru.maizy.cheesecake.server.checker.{ CheckResult, CheckStatus, HttpCheckResult }
import ru.maizy.cheesecake.server.resultsstorage.{ AddEndpointCheckResults, AggregateType, AggregatedResults }
import ru.maizy.cheesecake.server.resultsstorage.{ AllEndpoints, ClearEndpointCheckResults, DurationResult }
import ru.maizy.cheesecake.server.resultsstorage.{ EndpointCheckResults, GetAggregatedResults, GetAllEndpoints }
import ru.maizy.cheesecake.server.resultsstorage.{ GetEndpointCheckResults, InMemoryResultStorageActor, IntResult }
import ru.maizy.cheesecake.server.resultsstorage.{ LastResultAggregate, OptionalDateTimeResult, OptionalStatusResult }
import ru.maizy.cheesecake.server.resultsstorage.SimpleAggregate
import ru.maizy.cheesecake.server.service.{ EndpointFQN, HttpEndpoint, Service, SymbolicAddress }
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

    val successfulCheckResults: IndexedSeq[HttpCheckResult] =
      Range(0, CHECKS_LIMIT + 5).map(i => checkResult((i + 1) * 10, CheckStatus.Ok))


    val lastUnavailable = LastResultAggregate(CheckStatus.Unavailable)
    val lastOk = LastResultAggregate(CheckStatus.Ok)
    val lastUnableToCheck = LastResultAggregate(CheckStatus.UnableToCheck)
    val uptimeChecks = SimpleAggregate(AggregateType.UptimeChecks)
    val uptimeDuration = SimpleAggregate(AggregateType.UptimeDuration)
    val currentStatus = SimpleAggregate(AggregateType.CurrentStatus)
    val currentExtraInfo = SimpleAggregate(AggregateType.CurrentExtraInfo)
    val allAggregates = Seq(
      lastUnavailable,
      lastOk,
      lastUnableToCheck,
      uptimeChecks,
      uptimeDuration,
      currentStatus,
      currentExtraInfo
    )

    def checkResult(shift: Int, status: CheckStatus.Type): HttpCheckResult =
      HttpCheckResult(
        status,
        baseTime.plus(Duration.of(shift.toLong, ChronoUnit.MILLIS))
      )
  }

  trait WithSyncActorAndSampleData extends SampleData {
    val syncRef = TestActorRef[InMemoryResultStorageActor](InMemoryResultStorageActor.props())

    def getResults(endpointFqn: EndpointFQN): Option[InMemoryResultStorageActor#ResultData] = {
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
      expectMsg(AggregatedResults(Map(
        endpointFqn -> Map.empty,
        otherEndpointFqn -> Map.empty
      )))

      ref ! GetAggregatedResults(Seq.empty, allAggregates)
      expectMsg(AggregatedResults(Map.empty))

      // TODO: an empty result aggregate with an existing endpoint
    }
  }

  it should "returns Last*" in {
    new SampleData {
      val timeout = 3.seconds   // may be increased for a debugger session

      val timeShift = Stream.iterate(0)(_ + 1).iterator
      for (status <- CheckStatus.values) {
        println(s"Check $status")
        val ref = system.actorOf(InMemoryResultStorageActor.props())
        val aggregate = LastResultAggregate(status)
        val resultBuilder = () => checkResult(timeShift.next(), status)
        val otherResultsBuilder: () => Seq[HttpCheckResult] = () =>
          CheckStatus.values.filterNot(_ == status).map(checkResult(timeShift.next(), _)).toSeq

        // add other check status types
        ref ! AddEndpointCheckResults(endpointFqn, otherResultsBuilder())

        // should not affect the current aggregate
        ref ! GetAggregatedResults(Seq(endpointFqn), Seq(aggregate))
        expectMsg(timeout, AggregatedResults(Map(
          endpointFqn -> Map(
            aggregate ->  OptionalDateTimeResult(None)
          )
        )))

        // add current type check
        val result = resultBuilder()
        ref ! AddEndpointCheckResults(endpointFqn, Seq(result))

        // should returns it as a result
        ref ! GetAggregatedResults(Seq(endpointFqn), Seq(aggregate))
        expectMsg(timeout, AggregatedResults(Map(
          endpointFqn -> Map(
            aggregate ->  OptionalDateTimeResult(Some(result.checkTime))
          )
        )))

        // add other check status types ...
        ref ! AddEndpointCheckResults(endpointFqn, otherResultsBuilder())

        // should not affect the current aggregate
        ref ! GetAggregatedResults(Seq(endpointFqn), Seq(aggregate))
        expectMsg(timeout, AggregatedResults(Map(
          endpointFqn -> Map(
            aggregate ->  OptionalDateTimeResult(Some(result.checkTime))
          )
        )))

        // an old result should replaced by the new result
        val newerResult = resultBuilder()
        ref ! AddEndpointCheckResults(endpointFqn, Seq(newerResult))

        ref ! GetAggregatedResults(Seq(endpointFqn), Seq(aggregate))
        expectMsg(timeout, AggregatedResults(Map(
          endpointFqn -> Map(
            aggregate ->  OptionalDateTimeResult(Some(newerResult.checkTime))
          )
        )))
      }
    }
  }

  it should "returns uptime checks & duration" in {
    val timeout = 3.seconds   // may be increased for a debugger session
    val timeShift = Stream.iterate(0)(_ + 1).iterator

    new WithAsyncActorAndSampleData {
      ref ! GetAggregatedResults(Seq(endpointFqn), Seq(uptimeChecks, uptimeDuration))
      expectMsg(timeout, AggregatedResults(Map(
        endpointFqn -> Map(
          uptimeChecks -> IntResult(0),
          uptimeDuration -> DurationResult(Duration.ZERO)
        )
      )))

      val successChecks = Range(0, 3).map(_ => checkResult(timeShift.next(), CheckStatus.Ok))
      ref ! AddEndpointCheckResults(endpointFqn, successChecks)

      ref ! GetAggregatedResults(Seq(endpointFqn), Seq(uptimeChecks, uptimeDuration))

      val msg = expectMsgClass(timeout, classOf[AggregatedResults])
      val endpointRes = msg.results(endpointFqn)
      endpointRes(uptimeChecks).asInstanceOf[IntResult] shouldBe IntResult(3)
      endpointRes(uptimeDuration).asInstanceOf[DurationResult].result should be >= Duration.between(
        successfulCheckResults(0).checkTime, ZonedDateTime.now())

      ref ! AddEndpointCheckResults(endpointFqn, Seq(checkResult(timeShift.next(), CheckStatus.Unavailable)))

      ref ! GetAggregatedResults(Seq(endpointFqn), Seq(uptimeChecks, uptimeDuration))
      expectMsg(timeout, AggregatedResults(Map(
        endpointFqn -> Map(
          uptimeChecks -> IntResult(0),
          uptimeDuration -> DurationResult(Duration.ZERO)
      ))))
    }
  }

  it should "returns CurrentStatus" in {
    val timeout = 3.seconds   // may be increased for a debugger session

    new WithAsyncActorAndSampleData {

      ref ! GetAggregatedResults(Seq(endpointFqn), Seq(currentStatus))
      expectMsg(timeout, AggregatedResults(Map(
        endpointFqn -> Map(
          currentStatus -> OptionalStatusResult(None)
        )
      )))

      ref ! AddEndpointCheckResults(endpointFqn, Seq(checkResult(0, CheckStatus.Ok)))
      ref ! GetAggregatedResults(Seq(endpointFqn), Seq(currentStatus))
      expectMsg(timeout, AggregatedResults(Map(
        endpointFqn -> Map(
          currentStatus -> OptionalStatusResult(Some(CheckStatus.Ok))
        )
      )))

      ref ! AddEndpointCheckResults(endpointFqn, Seq(checkResult(10, CheckStatus.UnableToCheck)))
      ref ! GetAggregatedResults(Seq(endpointFqn), Seq(currentStatus))
      expectMsg(timeout, AggregatedResults(Map(
        endpointFqn -> Map(
          currentStatus -> OptionalStatusResult(Some(CheckStatus.UnableToCheck))
        )
      )))
    }
  }

  "InMemoryResultStorage ←GetEndpointCheckResults" should "return empty results for empty requests" in {
    new WithAsyncActorAndSampleData {
      ref ! GetEndpointCheckResults(Seq.empty, limit = 5)
      expectMsg(EndpointCheckResults(Map.empty))

      ref ! GetEndpointCheckResults(Seq(endpointFqn, otherEndpointFqn), limit = 0)
      expectMsg(EndpointCheckResults(Map(
        endpointFqn -> IndexedSeq.empty,
        otherEndpointFqn -> IndexedSeq.empty
      )))
    }
  }

  it should "return all stored results" in {
    new WithAsyncActorAndSampleData {
      ref ! AddEndpointCheckResults(endpointFqn, successfulCheckResults)
      ref ! AddEndpointCheckResults(otherEndpointFqn, successfulCheckResults.take(3))
      ref ! GetEndpointCheckResults(Seq(otherEndpointFqn, endpointFqn))
      expectMsg(EndpointCheckResults(Map(
        otherEndpointFqn -> successfulCheckResults.take(3).reverse,
        endpointFqn -> successfulCheckResults.takeRight(CHECKS_LIMIT).reverse
      )))
    }
  }

  it should "return limited results" in {
    new WithAsyncActorAndSampleData {
      ref ! AddEndpointCheckResults(endpointFqn, successfulCheckResults)
      ref ! AddEndpointCheckResults(otherEndpointFqn, successfulCheckResults.take(5))

      ref ! GetEndpointCheckResults(Seq(endpointFqn, otherEndpointFqn), limit = 3)
      expectMsg(EndpointCheckResults(Map(
        endpointFqn -> successfulCheckResults.takeRight(3).reverse,
        otherEndpointFqn -> successfulCheckResults.slice(2, 5).reverse
      )))
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
