package ru.maizy.cheesecake.server.jsonapi.models

import ru.maizy.cheesecake.server.service.EndpointFQN

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

// FIXME: tmp format
case class FullView(endpoints: Seq[EndpointFQN], tmpToStringRes: String = "world")
