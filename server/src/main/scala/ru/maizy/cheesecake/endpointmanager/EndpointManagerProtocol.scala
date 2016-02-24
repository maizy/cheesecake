package ru.maizy.cheesecake.endpointmanager

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import scala.concurrent.duration.FiniteDuration

trait EndpointManagerProtocol

case class SetCheckInterval(interval: FiniteDuration)
case object DisableChecking
case object Check
