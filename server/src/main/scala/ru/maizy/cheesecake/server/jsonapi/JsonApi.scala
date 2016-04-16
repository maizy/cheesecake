package ru.maizy.cheesecake.server.jsonapi

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import scala.concurrent.{ ExecutionContext, Future }
import scala.concurrent.duration.DurationInt
import akka.actor.{ ActorRef, ActorSystem }
import akka.pattern.ask
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import ru.maizy.cheesecake.server.{ BuildInfo, Version }
import ru.maizy.cheesecake.server.checker.CheckStatus
import ru.maizy.cheesecake.server.jsonapi.models.{ AppConfigs, AppVersion, FullView }
import ru.maizy.cheesecake.server.resultsstorage.{ AggregateType, AggregatedResults, AllEndpoints }
import ru.maizy.cheesecake.server.resultsstorage.{ GetAggregatedResults, GetAllEndpoints, LastResultAggregate }
import ru.maizy.cheesecake.server.resultsstorage.SimpleAggregate


class JsonApi(system: ActorSystem, host: String, port: Int) extends JsonApiMarshallers {
  implicit val ec: ExecutionContext = system.dispatcher
  implicit val timeout: Timeout = 20.seconds

  private var _storageRef: Option[ActorRef] = None

  private val services: Route =
    path("state" / "full_view") {
      get {
        complete {
          fullView()
        }
      }
    }

  private val configs: Route =
    path("configs") {
      get {
        complete {
          AppConfigs(wsStateUrl = "/ws/state")
        }
      }
    }

  private val status: Route =
    path("version") {
      get {
        complete {
          AppVersion(Version.literal, BuildInfo.buildTime)
        }
      }
    } ~ path("status") {
      get {
        complete("ok")
      }
    }

  val routes: Route = logRequestResult("cheesecake-json-api") {
    configs ~
    status ~
    pathPrefix("services")(services)
  }

  def storageRef: Future[ActorRef] =
    if (_storageRef.isEmpty) {
      val future = system.actorSelection("/user/storage").resolveOne
      future.onSuccess {
        case ref: ActorRef => _storageRef = Some(ref)
      }
      future
    } else {
      Future.successful(_storageRef.get)
    }

  def fullView(): Future[FullView] = storageRef.flatMap { storage =>
    for(
      endpoints <- (storage ? GetAllEndpoints).mapTo[AllEndpoints];
      aggregates <- (storage ? GetAggregatedResults(
        endpoints.endpointsFqns,
        Seq(
          SimpleAggregate(AggregateType.UptimeDuration),
          SimpleAggregate(AggregateType.UptimeChecks),
          SimpleAggregate(AggregateType.CurrentStatus),
          LastResultAggregate(CheckStatus.Ok),
          LastResultAggregate(CheckStatus.Unavailable),
          LastResultAggregate(CheckStatus.UnableToCheck)
        )
      )).mapTo[AggregatedResults]
    ) yield FullView.groupAggregatedResults(aggregates.results)
  }
}
