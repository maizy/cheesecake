package ru.maizy.cheesecake.utils

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import java.net.URLEncoder

object ActorUtils {
  def escapeActorName(name: String): String = URLEncoder.encode(name, "utf-8")
}
