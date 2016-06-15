package ru.maizy.cheesecake.server.resultsstorage

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import java.time.{ Duration, ZonedDateTime }
import scala.collection.immutable.Queue
import scala.collection.mutable
import akka.actor.{ Props, ActorLogging }
import akka.event.LoggingReceive
import ru.maizy.cheesecake.core.utils.LimitedQueue._
import ru.maizy.cheesecake.server.checker.{ CheckResult, CheckStatus }
import ru.maizy.cheesecake.server.service.EndpointFQN

class InMemoryResultStorageActor extends ResultStorageActor with ActorLogging {

  private[cheesecake] class ResultData() {

    var checks: Queue[CheckResult] = Queue.empty
    val lastStatus: mutable.Map[CheckStatus.Type, ZonedDateTime] = mutable.Map.empty
    var uptimeChecks: Int = 0
    var uptimeUntil: Option[ZonedDateTime] = None

    // TODO: make those vars configurable
    val checksLimit: Int = 10
    val maxFutureShift = 2000  // ms
    val maxPastShift = 60 * 1000 // ms

    def uptimeDuration(to: ZonedDateTime = ZonedDateTime.now()): Option[Duration] =
      uptimeUntil.map(Duration.between(_, to))

    def addCheck(check: CheckResult): Unit = {
      isValid(check) match {
        case Left(_) =>
          appendCheck(check)
          recomputeAggregates(check)

        case Right(err) =>
          log.warning(s"Not valid check result $check: $err")
      }
    }

    protected def isValid(check: CheckResult): Either[Boolean, String] = {
      val now = ZonedDateTime.now()
      val shift = Duration.between(check.checkTime, now)

      val absShift = shift.toMillis.abs
      if (shift.isNegative && absShift > maxFutureShift) {
        Right(s"Too far in the future (${absShift}ms)")
      } else if (!shift.isNegative && absShift > maxPastShift) {
        Right(s"Too far in the past (${absShift}ms)")
      } else {
        Left(true)
      }
    }

    protected def appendCheck(check: CheckResult): Unit = {
      // empty queue or prepend
      if (checks.isEmpty || checks.head.checkTime.isBefore(check.checkTime)) {
        checks = checks.enqueueWithLimit(check, checksLimit)
      } else {
        // TODO: reorder messages (iss #16)
        log.warning(s"Skip check result because it from the past: $check (head time: ${checks.head.checkTime})")
      }
    }

    def clearChecks(): Unit = {
      checks = Queue.empty
    }

    protected def recomputeAggregates(check: CheckResult): Unit = {
      val status  = check.status
      if (lastStatus.get(status).isEmpty || lastStatus(status).isBefore(check.checkTime)) {
        lastStatus(status) = check.checkTime
      }
      if (status == CheckStatus.Ok) {
        if (uptimeUntil.isEmpty || uptimeUntil.get.isAfter(check.checkTime)) {
          uptimeUntil = Some(check.checkTime)
        }
        uptimeChecks += 1
      } else {
        // TODO: recount by an iteration, currenty assumed that the new check last in the queue (iss #16)
        uptimeChecks = 0
        uptimeUntil = None
      }
    }
  }

  private[cheesecake] val results: mutable.Map[EndpointFQN, ResultData] = mutable.Map.empty

  override def receive: Receive = LoggingReceive(
      handlerDataMessages orElse handlerRequestMessages
    )

  private def handlerDataMessages: Receive = {
    case AddEndpointCheckResults(endpointFqn, newResults) =>
      results.getOrElseUpdate(endpointFqn, new ResultData())
      newResults.foreach(results(endpointFqn).addCheck)

    case ClearEndpointCheckResults(endpointFqn) =>
      results.get(endpointFqn).foreach(_.clearChecks())
  }

  private def handlerRequestMessages: Receive = {
    case GetAllEndpoints =>
      sender() ! AllEndpoints(results.keys.toSeq)

    case GetAllServices =>
      sender() ! AllServices(results.keys.map(_.service).toSeq)

    case GetAggregatedResults(endpointFqns, aggregates) =>
      val res = for (endpoint <- endpointFqns) yield endpoint -> getEndpointAggregates(endpoint, aggregates)
      sender() ! AggregatedResults(res.toMap)

    case GetEndpointCheckResults(endpointsFqn, limit) =>
      val res = endpointsFqn.map { endpointFqn =>
        results.get(endpointFqn) match {
          case Some(endpointResults) =>
            endpointFqn -> endpointResults.checks.takeRight(limit).reverse.toIndexedSeq
          case None =>
            endpointFqn -> IndexedSeq.empty
        }
      }.toMap
      sender() ! EndpointCheckResults(res)

  }

  def getEndpointAggregates(endpoint: EndpointFQN, aggregates: Seq[Aggregate]): Map[Aggregate, AggregateResult[Any]] = {
    val pairs = for(
      aggregate <- aggregates;
      res <- getAggregate(endpoint, aggregate) match {
        case Left(error) =>
          log.warning(s"Unable to get aggregate $aggregate for $endpoint: $error")
          None
        case Right(result) =>
          Some(result)
      }
    ) yield aggregate -> res
    pairs.toMap
  }

  def getAggregate(endpoint: EndpointFQN, aggregate: Aggregate): Either[String, AggregateResult[Any]] = {
    val cell = results.get(endpoint)
    aggregate match {
      case LastResultAggregate(status) =>
        Right(OptionalDateTimeResult(
          cell.flatMap(_.lastStatus.get(status))
        ))

      case SimpleAggregate(AggregateType.UptimeChecks) =>
        Right(IntResult(
          cell.map(_.uptimeChecks).getOrElse(0)
        ))

      case SimpleAggregate(AggregateType.UptimeDuration) =>
        Right(DurationResult(
          cell
            .flatMap(_.uptimeUntil)
            .map { r => Duration.between(r, ZonedDateTime.now()) }
            .getOrElse(Duration.ZERO)
        ))

      case SimpleAggregate(AggregateType.CurrentStatus) =>
        Right(OptionalStatusResult(
          cell
            .flatMap(_.checks.lastOption)
            .map(_.status)
        ))

      case SimpleAggregate(AggregateType.CurrentExtraInfo) =>
        Right(OptionalExtraInfoResult(
          cell
            .flatMap(_.checks.lastOption)
            .flatMap(_.extraInfo)
        ))

      case other: Aggregate => Left(s"Unknown aggregate: $other")
    }
  }
}

object InMemoryResultStorageActor {
  def props(): Props = Props(new InMemoryResultStorageActor)
}
