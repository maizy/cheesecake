package ru.maizy.cheesecake

/**
  * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
  * See LICENSE.txt for details.
  */
object Version {
  val asSeq: Seq[Int] = Seq(0, 0, 1)
  val literal: String = asSeq.mkString(".")

  override def toString: String = literal
}
