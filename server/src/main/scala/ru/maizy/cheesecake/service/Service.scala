package ru.maizy.cheesecake.service

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

case class Service(name: String)

case class EndpointFQN(service: Service, endpoint: Endpoint) {
  override def toString: String = s"Service(${service.name})/Endpoint($endpoint)"
}
