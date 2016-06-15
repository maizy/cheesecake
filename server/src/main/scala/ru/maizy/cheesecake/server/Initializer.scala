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
import ru.maizy.cheesecake.core.RichTypesafeConfig.{ ConfigListImplicits, IntImplicits }
import ru.maizy.cheesecake.core.RichTypesafeConfig.{ ObjectImplicits, StringImplicits }
import ru.maizy.cheesecake.core.WarnMessages
import ru.maizy.cheesecake.server.bodyparser.{ BodyParserSpec, JsonParserSpec, RegexpParserSpec }
import ru.maizy.cheesecake.server.bodyparser.{ TextParserSpec, XmlParserSpec }

case class ExtractResult[+T](warnings: WarnMessages, result: Option[T])

object ExtractResult {
  def singleError[T](message: String): ExtractResult[T] = ExtractResult[T](Seq(message), None)
  def successWithoutWarnings[T](result: T): ExtractResult[T] = ExtractResult(Seq.empty, Some(result))
}

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

  private def extractService(serviceConfig: Config): ExtractResult[Service] = {
    val eitherName = serviceConfig.eitherString("name")
    val maybeService = eitherName
      .right.map(Service(_))
      .right.toOption
    val warnings = eitherName.left.toSeq.map("`name` field warning: " + _.errorMessage)
    ExtractResult(warnings, maybeService)
  }

  private def extractAddress(addressConfig: Config): ExtractResult[HttpAddress] = {
    val maybeIp = addressConfig.optString("ip")
    val maybeHost = addressConfig.optString("host")
    var warnings: WarnMessages = Seq.empty
    val maybeAddress = (maybeIp, maybeHost) match {
      case (Some(ip), None) => Some(IpAddress(InetAddress.getByName(ip)))  // TODO: check ip format
      case (None, Some(host)) => Some(SymbolicAddress(host))  // TODO: check hostname validity
      case (None, None) => Some(SymbolicAddress("localhost"))
      case _ =>
        warnings = warnings :+ "Both ip & host defined, skipping"
        None
    }
    ExtractResult(warnings, maybeAddress)
  }

  private def extractBodyParserSpec(parserConfig: Config): ExtractResult[BodyParserSpec] = {
    parserConfig.optString("type") match {
      case Some("text") => ExtractResult.successWithoutWarnings(TextParserSpec)
      case Some("regexp") => RegexpParserSpec.fromConfig(parserConfig)
      case Some("json") => JsonParserSpec.fromConfig(parserConfig)
      case Some("xml") => XmlParserSpec.fromConfig(parserConfig)
      case Some(unknownType) => ExtractResult.singleError(s"Unknown body parser type $unknownType")
      case _ => ExtractResult.singleError("Body parser type not specified")
    }
  }

  private def extractBodyParsers(endpointConfig: Config): ExtractResult[Map[String, BodyParserSpec]] = {
    val parsersConfigsRes = endpointConfig.eitherConfigObjectMapWithWarnings("body_parsers")

    var warnings = parsersConfigsRes.warnings.map("Body parsers warning: " + _)
    var parsers: Map[String, BodyParserSpec] = Map.empty
    for (
      parsersMap <- parsersConfigsRes.result.right.toSeq;
      (key, parserConfigObject) <- parsersMap
    ) {
      val ExtractResult(parserWarnings, maybeParser) = extractBodyParserSpec(parserConfigObject.toConfig)
      warnings ++= parserWarnings.map(s"Body parser `$key` warning: " + _)
      maybeParser foreach { parser => parsers += (key -> parser) }
    }
    ExtractResult(warnings, Some(parsers).filterNot(_.isEmpty))
  }

  private def extractHeaders(endpointConfig: Config): ExtractResult[Headers] = {
    val headersResult = endpointConfig.eitherStringMapWithWarnings("headers")
    val maybeHeaders = headersResult.result.right.toOption.map(_.mapValues(Seq(_)))
    ExtractResult(headersResult.warnings, maybeHeaders)
  }

  private def extractEndpoint(endpointConfig: Config): ExtractResult[Endpoint] = {
    val endpointType = endpointConfig.optString("type").getOrElse("http")
    endpointType match {
      case "http" =>
        val port = endpointConfig.optInt("port").getOrElse(80)
        val path = endpointConfig.optString("path").getOrElse("/")

        var warnings: WarnMessages = Seq.empty

        val ExtractResult(headersWarnings, maybeHeaders) = extractHeaders(endpointConfig)
        warnings ++= headersWarnings.map("Headers warning: " + _)

        val ExtractResult(bodyParsersWarnings, maybeParsers) = extractBodyParsers(endpointConfig)
        warnings ++= bodyParsersWarnings.map("Parsers warning: " + _)

        val ExtractResult(addressWarnings, maybeAddress) = extractAddress(endpointConfig)
        warnings ++= addressWarnings.map("Address warning: " + _)

        val maybeEndpoint: Option[Endpoint] = maybeAddress match {
          case Some(address) =>
            Some(HttpEndpoint(address, port, path, maybeHeaders, maybeParsers))
          case None =>
            warnings = warnings :+ "Unable to detect address, skipping"
            None
        }

        ExtractResult(warnings, maybeEndpoint)
      case other: String =>
        ExtractResult.singleError(s"Unsupported endpoint type `$other`")
    }
  }

  private def extractEndpoints(serviceConfig: Config): ExtractResult[Set[Endpoint]] = {
    val eitherEndpoints = serviceConfig.eitherConfigList("endpoints", allowEmpty = true)
    var warnings: WarnMessages = Seq.empty[String]
    warnings ++= eitherEndpoints.left.toSeq.map { error =>
      s"Error on looking for endpoints: $error"
    }

    var endpoints: Set[Endpoint] = Set.empty
    for (
      endpointsConfigs <- eitherEndpoints.right.toSeq;
      (endpointConfig, index) <- endpointsConfigs.zipWithIndex
    ) {
      val ExtractResult(endpointWarnings, maybeEndpoint) = extractEndpoint(endpointConfig)
      warnings ++= endpointWarnings.map { w =>
        s"Warning in endpoint #$index: $w"
      }
      maybeEndpoint match {
        case None => warnings = warnings :+ s"Unable to initialize endpoint #$index"
        case Some(endpoint) => endpoints = endpoints + endpoint
      }
    }

    ExtractResult(warnings, Some(endpoints).filterNot(_.isEmpty))
  }

  def fromConfig(config: Config)(implicit materializer: ActorMaterializer): Unit = {
    val storageRef = buildStorage()
    val httpChecker = buildHttpChecker()

    val services  = config.eitherConfigList("cheesecake.services", allowEmpty = true)
    services.left.foreach { error => logger.warn(s"Error on looking for services in config: $error") }
    for (
      services <- services.right;
      (serviceConfig, index) <- services.zipWithIndex
    ) {
      val ExtractResult(serviceWarnings, maybeService) = extractService(serviceConfig)
      serviceWarnings.foreach { error => logger.warn(s"Warning for service #$index: $error") }
      maybeService match {
        case None => logger.warn(s"Unable to initialize service #$index, skipping")
        case Some(service) =>
          val ExtractResult(warnings, maybeEndpoints) = extractEndpoints(serviceConfig)
          warnings.foreach { w => logger.warn(s"Warning for endpoints in service #$index: $w") }
          maybeEndpoints match {
            case None => logger.warn(s"No endpoints in service #$index, skipping")
            case Some(endpoints) =>
            val serviceActor = buildServiceActor(service, storageRef, httpChecker)
            serviceActor ! AddEndpoints(endpoints)
          }
      }
    }
  }
}
