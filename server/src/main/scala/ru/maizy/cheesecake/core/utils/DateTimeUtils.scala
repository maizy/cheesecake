package ru.maizy.cheesecake.core.utils

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import java.time.format.DateTimeFormatter

object DateTimeUtils {
  val humanReadableDateTimeFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")
}
