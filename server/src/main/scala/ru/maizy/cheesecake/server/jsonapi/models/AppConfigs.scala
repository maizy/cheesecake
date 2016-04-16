package ru.maizy.cheesecake.server.jsonapi.models

import java.time.ZonedDateTime

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */
case class AppConfigs(wsStateUrl: String)
case class AppVersion(version: String, buildTime: ZonedDateTime)
