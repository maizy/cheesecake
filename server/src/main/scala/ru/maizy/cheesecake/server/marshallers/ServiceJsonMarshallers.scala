package ru.maizy.cheesecake.server.marshallers

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import ru.maizy.cheesecake.core.utils.StringUtils
import spray.json.{ JsNumber, JsObject, JsString, JsValue, RootJsonFormat, pimpAny }
import ru.maizy.cheesecake.server.service.{ Endpoint, EndpointFQN, HttpAddress, HttpEndpoint, IpAddress, Service }
import ru.maizy.cheesecake.server.service.SymbolicAddress

// TODO: how to use only writer for case classes
trait ServiceJsonMarshallers extends JsonMarshaller {
  implicit val serviceFormat = jsonFormat1(Service)

  implicit object IpAddressFormat extends RootJsonFormat[IpAddress] {
    def write(ip: IpAddress): JsValue = JsObject(
        "type" -> JsString("ip"),
        "ip" -> JsString(ip.hostName),
        "hostname" -> JsString(ip.hostName)
      )
    override def read(json: JsValue): IpAddress = ???
  }

  implicit object SymbolicAddressFormat extends RootJsonFormat[SymbolicAddress] {
    def write(ip: SymbolicAddress): JsValue = JsObject(
        "type" -> JsString("symbolic"),
        "hostname" -> JsString(ip.hostName)
      )
    override def read(json: JsValue): SymbolicAddress = ???
  }

  implicit object HttpAddressFormat extends RootJsonFormat[HttpAddress] {
    def write(address: HttpAddress): JsValue = address match {
      case ip: IpAddress => ip.toJson
      case symb: SymbolicAddress => symb.toJson
      case _ => JsObject("hostname" -> JsString(address.hostName))
    }
    override def read(json: JsValue): HttpAddress = ???
  }

  implicit object HttpEndpointFormat extends RootJsonFormat[HttpEndpoint] {
    def write(endpoint: HttpEndpoint): JsValue = JsObject(
        "type" -> JsString("http"),
        "address" -> endpoint.address.toJson,
        "port" -> JsNumber(endpoint.port),
        "path" -> JsString(endpoint.path),
        "headers" -> endpoint.headers.toJson,
        "parsers" -> JsObject(endpoint
          .bodyParsers
          .getOrElse(Map.empty)
          .mapValues(spec => JsObject(
            // FIXME: tmp (add parser formatter with all types support)
            "type" -> JsString(StringUtils.upperCaseToDashes(spec.parserType.toString)))
          )
        )
      )
    override def read(json: JsValue): HttpEndpoint = ???
  }

  implicit object EndpointFormat extends RootJsonFormat[Endpoint] {
    def write(address: Endpoint): JsValue = address match {
      case ep: HttpEndpoint => ep.toJson
      case _ => JsObject(
        "type" -> JsString("unknown")
      )
    }

    override def read(json: JsValue): Endpoint = ???
  }

  implicit val endpointFQN = jsonFormat2(EndpointFQN)
}
