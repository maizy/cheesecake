package ru.maizy.cheesecake.endpointmanager

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import scala.concurrent.duration.FiniteDuration

sealed trait EndpointManagerProtocol

case class SetCheckInterval(interval: FiniteDuration) extends EndpointManagerProtocol
case object DisableChecking extends EndpointManagerProtocol
case object Check extends EndpointManagerProtocol
