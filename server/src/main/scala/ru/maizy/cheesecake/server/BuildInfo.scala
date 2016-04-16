package ru.maizy.cheesecake.server

import java.time.{ Instant, ZoneId, ZonedDateTime }
import java.util.Properties

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */
case object BuildInfo {

  private val buildProperties: Map[String, Option[String]] = {
    val props = new Properties
    Option(getClass.getClassLoader.getResourceAsStream("buildinfo.properties"))
      .foreach(props.load)
    Map(
      "version" -> Option(props.getProperty("version")),
      "name" -> Option(props.getProperty("name")),
      "buildTime" -> Option(props.getProperty("buildTime"))
    )
  }

  def version: String = buildProperties("version").getOrElse("0.0.0")
  def projectName: String = buildProperties("name").getOrElse("cheesecake-server")
  def buildTime: ZonedDateTime = {
    val utc = ZoneId.of("UTC")
    buildProperties("buildTime")
      .map { t => ZonedDateTime.ofInstant(Instant.ofEpochMilli(t.toLong), utc) }
      .getOrElse(ZonedDateTime.now().withZoneSameInstant(utc))
  }

}
