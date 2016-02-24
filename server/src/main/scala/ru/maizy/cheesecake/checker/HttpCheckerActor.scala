package ru.maizy.cheesecake.checker

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import scala.concurrent.duration._
import scala.concurrent.Future
import akka.actor.{ ActorRef, ActorLogging, Actor }
import akka.http.scaladsl.settings.ClientConnectionSettings
import akka.http.scaladsl.model.{ HttpResponse, HttpRequest, StatusCodes }
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{ Sink, Source, Flow }
import akka.pattern.pipe
import com.typesafe.config.{ Config, ConfigFactory }
import ru.maizy.cheesecake.utils.CollectionsUtils
import ru.maizy.cheesecake.{ Headers, Timestamp }
import ru.maizy.cheesecake.service.HttpEndpoint


class HttpCheckerActor(val m: ActorMaterializer) extends Actor with ActorLogging {

  implicit val materializer = m  // TODO: is there any better solution?
  implicit val system = context.system
  implicit val ec = system.dispatcher  // TODO: separate dispatcher

  val connectTimeout = 1.seconds
  val readTimeout = 2.seconds
  val idleTimeout = 1.seconds

  val config: Config = context.system.settings.config
  val defaultConfigOverrides: Config = ConfigFactory.parseString(
    s"""
      |akka.http.client.connecting-timeout = ${connectTimeout.toSeconds}s
      |akka.http.client.idle-timeout = ${idleTimeout.toSeconds}s
    """.stripMargin)

  override def receive: Receive = {
    case HttpCheck(endpoint, includeResponse) =>
      check(endpoint, this.sender(), includeResponse)

    case Check(endpoint: HttpEndpoint) =>
      check(endpoint, this.sender(), includeResponse = false)

    case c@Check(_) => log.error(s"Unknow check type: $c")
  }

  def check(endpoint: HttpEndpoint, sender: ActorRef, includeResponse: Boolean = false): Unit = {
    log.info(s"check http endpoint: $endpoint" + (if (includeResponse) " with response" else ""))
    val checkResult: Future[HttpCheckResult] =
      Source.single(HttpRequest(uri = endpoint.path))
        .via(buildFlow(endpoint))
        .runWith(Sink.head)
        .flatMap(checkResponse(_, includeResponse, endpoint))
        .recoverWith {
          // TODO: detect failure reason (DNS, connection failed, timeout ...)
          case e: Throwable => Future.successful(HttpCheckResult(endpoint, CheckStatus.UnableToCheck, Timestamp.now()))
        }
    checkResult pipeTo sender
  }

  def buildConnectionSettings(endpoint: HttpEndpoint): ClientConnectionSettings = {
    // TODO: configurable for each endpoint
    ClientConnectionSettings(defaultConfigOverrides.withFallback(config))
  }

  def buildFlow(endpoint: HttpEndpoint): Flow[HttpRequest, HttpResponse, Future[Http.OutgoingConnection]] = {
    // TODO: separate and explicit dns resolving
    Http().outgoingConnection(
      host = endpoint.address.hostName,
      port = endpoint.port,
      settings = buildConnectionSettings(endpoint)
    )
  }

  def checkResponse(
      response: HttpResponse,
      includeResponse: Boolean,
      endpoint: HttpEndpoint): Future[HttpCheckResult] = {
    val time = Timestamp.now()
    val checkStatus = response.status match {
      case StatusCodes.OK => CheckStatus.Ok
      case _ => CheckStatus.Unavailable
    }
    val headers: Option[Headers] = includeResponse match {
      case true =>
        Some(
          CollectionsUtils.tuplesToMultiMap(
            response.headers.map { h => (h.name, h.value) }
          )
        )
      case _ => None
    }
    val result = HttpCheckResult(
      endpoint,
      checkStatus,
      time,
      httpStatus = Some(response.status.intValue()),
      headers = headers
    )
    if (includeResponse) {
      response.entity
        .toStrict(readTimeout)
        .map { strictEntity =>
          result.copy(body = Some(strictEntity.data))
        }
        .recoverWith {
          // TODO: detect failure reason
          case e: Throwable => Future.successful(result.copy(status = CheckStatus.Unavailable))
        }
    } else {
      Future.successful(result)
    }
  }

}
