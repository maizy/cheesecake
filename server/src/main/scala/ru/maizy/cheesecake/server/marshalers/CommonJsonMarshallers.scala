package ru.maizy.cheesecake.server.marshalers

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{ DefaultJsonProtocol, JsObject, JsString, JsValue, RootJsonFormat }
import spray.json.{ JsNumber, pimpAny }
import ru.maizy.cheesecake.server.service.{ Endpoint, EndpointFQN, HttpAddress, HttpEndpoint, IpAddress, Service}
import ru.maizy.cheesecake.server.service.SymbolicAddress

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

// TODO: how to use only writer for case classes
trait CommonJsonMarshallers extends SprayJsonSupport with DefaultJsonProtocol {
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
        "headers" -> endpoint.headers.toJson
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

  implicit val httpEndpointFormat = jsonFormat4(HttpEndpoint)

  implicit val endpointFQN = jsonFormat2(EndpointFQN)
}
