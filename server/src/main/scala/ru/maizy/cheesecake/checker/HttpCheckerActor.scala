package ru.maizy.cheesecake.checker

/**
  * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
  * See LICENSE.txt for details.
  */

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
import ru.maizy.cheesecake.{ Headers, Version, Timestamp }
import ru.maizy.cheesecake.service.HttpEndpoint


class HttpCheckerActor(val m: ActorMaterializer) extends Actor with ActorLogging {

  implicit val materializer = m  // TODO: is there any better solution?
  implicit val system = context.system  // TODO: separate dispatcher
  implicit val ec = system.dispatcher  // TODO: separate dispatcher

  val config: Config = context.system.settings.config

  val defaultConfigOverrides: Config = ConfigFactory.parseString(
    s"""
      |akka.http.client.user-agent-header = cheesecake/${Version.literal}
      |akka.http.client.connecting-timeout = 1s
      |akka.http.client.idle-timeout = 15s
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
    val response: Future[HttpCheckResult] =
      Source.single(HttpRequest(uri = endpoint.path))
        .via(buildFlow(endpoint))
        .runWith(Sink.head)
        .map { response =>
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
          // TODO
          val body: Option[Seq[Byte]] = includeResponse match {
            case true => None
            case _ => None
          }
          HttpCheckResult(
            endpoint,
            checkStatus,
            time,
            httpStatus = Some(response.status.intValue()),
            headers = headers,
            body = body
          )
        }
        .recoverWith {
          // TODO: detect failure reason (DNS, connection failed, timeout ...)
          case e: Throwable => Future.successful(HttpCheckResult(endpoint, CheckStatus.UnableToCheck, Timestamp.now()))
        }
    response pipeTo sender
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

}
