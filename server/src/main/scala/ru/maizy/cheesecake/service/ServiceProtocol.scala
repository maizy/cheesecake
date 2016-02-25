package ru.maizy.cheesecake.service

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */


trait ServiceProtocol

case class AddEndpoints(endpoints: Set[Endpoint]) extends ServiceProtocol
case class RemoveEndpoints(endpoints: Set[Endpoint]) extends ServiceProtocol
case object RemoveAllEndpoints extends ServiceProtocol
