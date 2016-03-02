package ru.maizy.cheesecake.service

import ru.maizy.cheesecake.checker.CheckResult

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */


sealed trait ServiceProtocol

case class AddEndpoints(endpoints: Set[Endpoint]) extends ServiceProtocol
case class RemoveEndpoints(endpoints: Set[Endpoint]) extends ServiceProtocol
case object RemoveAllEndpoints extends ServiceProtocol

case class EndpointStatus(endpoint: Endpoint, checkResult: CheckResult)
