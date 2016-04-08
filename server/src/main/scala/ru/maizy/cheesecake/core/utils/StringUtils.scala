package ru.maizy.cheesecake.core.utils
/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import scala.util.matching.Regex.Match

object StringUtils {
  private val upperCaseChars = "A-Z"
  private val endWithUpperCase = s"([$upperCaseChars]+$$)".r
  private val upperCaseRe = s"([$upperCaseChars]*)([$upperCaseChars]+)(?=[^$upperCaseChars]+)".r


  def upperCaseToDashes(name: String): String = {
    // TODO: there are some issues, see tests for more
    val endFixed = endWithUpperCase replaceSomeIn (name, m => Some("-" + m.group(1).toLowerCase))
    upperCaseRe.replaceAllIn(endFixed, _ match { case f: Match =>
      val pre = f.group(1)
      val last = f.group(2)

      (if (pre != "") "-" + pre.toLowerCase else "") + "-" + last.toLowerCase
    }).stripPrefix("-")
  }
}
