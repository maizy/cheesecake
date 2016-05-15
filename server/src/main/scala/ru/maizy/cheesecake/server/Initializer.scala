package ru.maizy.cheesecake.server

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import java.net.InetAddress
import akka.actor.{ ActorRef, ActorSystem }
import akka.stream.ActorMaterializer
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import ru.maizy.cheesecake.server.checker.HttpCheckerActor
import ru.maizy.cheesecake.server.resultsstorage.InMemoryResultStorageActor
import ru.maizy.cheesecake.server.service.{ AddEndpoints, Endpoint, HttpAddress, HttpEndpoint, IpAddress, Service }
import ru.maizy.cheesecake.server.service.{ ServiceActor, SymbolicAddress }
import ru.maizy.cheesecake.server.utils.ActorUtils.escapeActorName
import ru.maizy.cheesecake.core.RichTypesafeConfig.{ ConfigError, ConfigListImplicits, IntImplicits, MissingValue }
import ru.maizy.cheesecake.core.RichTypesafeConfig.{ ObjectImplicits, StringImplicits, WarnMessages }

class Initializer (private val system: ActorSystem) extends LazyLogging {

  var _defaultStorageRef: Option[ActorRef] = None
  var _defaultHttpCheckerRef: Option[ActorRef] = None

  def defaultStorageRef: Option[ActorRef] = _defaultStorageRef
  def defaultHttpChecker: Option[ActorRef] = _defaultHttpCheckerRef

  private def buildStorage(): ActorRef = {
    val storageRef = system.actorOf(InMemoryResultStorageActor.props(), "storage")
    _defaultStorageRef = Some(storageRef)
    storageRef
  }

  private def buildHttpChecker()(implicit materializer: ActorMaterializer): ActorRef = {
    // TODO: checker pool
    val httpCheckerRef = system.actorOf(HttpCheckerActor.props(materializer), name = "http-checker-1")
    _defaultHttpCheckerRef = Some(httpCheckerRef)
    httpCheckerRef
  }

  private def buildServiceActor(service: Service, storage: ActorRef,
      httpChecker: ActorRef)(implicit materializer: ActorMaterializer): ActorRef = {
    system.actorOf(
      ServiceActor.props(service, storage, httpChecker, materializer),
      name = escapeActorName(s"service-${service.name}")
    )
  }

  private def extractAddress(addressConfig: Config): Either[String, HttpAddress] = {
    val maybeIp = addressConfig.optString("ip")
    val maybeHost = addressConfig.optString("host")
    (maybeIp, maybeHost) match {
      case (Some(ip), None) => Right(IpAddress(InetAddress.getByName(ip)))  // TODO: check ip format
      case (None, Some(host)) => Right(SymbolicAddress(host))  // TODO: check hostname validity
      case (None, None) => Right(IpAddress(InetAddress.getLocalHost))
      case _ => Left("Both ip & host defined")
    }
  }

  private def extractEndpoint(service: Service, endpointConfig: Config): (Either[String, Endpoint], WarnMessages) = {
    val endpointType = endpointConfig.optString("type").getOrElse("http")
    endpointType match {
      case "http" =>
        val port = endpointConfig.optInt("port").getOrElse(80)
        val path = endpointConfig.optString("path").getOrElse("/")
        val headersRes = endpointConfig.eitherStringMapWithWarnings("headers")
        val headers = headersRes.result.right.toOption.map(_.mapValues(Seq(_)))
        val warnings: Seq[String] = headersRes.result match {
          case Left(e: MissingValue) => Seq.empty
          case Left(e: ConfigError) => Seq("Headers warning: " + e.errorMessage)
          case _ => Seq.empty
        }
        val eitherEndpoint = extractAddress(endpointConfig)
          .left.map("Bad address for endpoint: " + _)
          .right.map(HttpEndpoint(_, port, path, headers))
        (eitherEndpoint, headersRes.warnings.map("Headers warning: " + _) ++ warnings)
      case other: String => (Left(s"Unsupported endpoint type `$other`"), Seq.empty)
    }
  }

  private def extractEndpoints(service: Service, serviceConfig: Config): (Set[Endpoint], WarnMessages) = {
    val eitherEndpoints = serviceConfig.eitherConfigList("endpoints", allowEmpty = true)
    var warnMessages = Seq.empty[String]
    warnMessages ++= eitherEndpoints.left.toSeq.map { error =>
      s"Error on looking for endpoints for service ${service.name}: $error"
    }

    def checkEndpoint(res: Either[String, Endpoint], index: Int): Option[Endpoint] = {
      res.left.foreach { error => logger.warn(s"Error in endpoint #$index for service ${service.name}: $error") }
      res.right.toOption
    }

    val endpoints = for (
      endpointsConfigs <- eitherEndpoints.right.toSeq;
      (endpointConfig, index) <- endpointsConfigs.zipWithIndex;
      eitherEndpoint = {
        val (eitherEndpoint, warnings) = extractEndpoint(service, endpointConfig)
        warnMessages ++= warnings.map { w =>
          s"Warning in endpoint #$index for service ${service.name}: $w"
        }
        eitherEndpoint
      };
      endpoint <- checkEndpoint(eitherEndpoint, index)
    ) yield endpoint

    (endpoints.toSet, warnMessages)
  }

  private def extractService(serviceConfig: Config): Either[String, Service] =
    serviceConfig.eitherString("name")
      .right.map(Service(_))
      .left.map(_.errorMessage)

  def fromConfig(config: Config)(implicit materializer: ActorMaterializer): Unit = {
    val storageRef = buildStorage()
    val httpChecker = buildHttpChecker()

    val services  = config.eitherConfigList("cheesecake.services", allowEmpty = true)
    services.left.foreach { error => logger.warn(s"Error on looking for services in config: $error") }
    for (
      services <- services.right;
      (serviceConfig, index) <- services.zipWithIndex
    ) {
      val eitherService = extractService(serviceConfig)
      eitherService.left.foreach { error => logger.warn(s"Skip service #$index because of error: $error") }
      eitherService.right.foreach { service =>
        val (endpoints, warnings) = extractEndpoints(service, serviceConfig)
        warnings.foreach { w => logger.warn(w) }
        val serviceActor = buildServiceActor(service, storageRef, httpChecker)
        if (endpoints.nonEmpty) {
          serviceActor ! AddEndpoints(endpoints)
        }
      }
    }
  }
}
