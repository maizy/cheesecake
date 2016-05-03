package ru.maizy.cheesecake.server

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */


object Version {

  val literal: String = BuildInfo.version
  val asSeq: Seq[Int] = literal.split(".").map(_.toInt)

  override def toString: String = literal
}
