package ru.maizy.cheesecake.server

import java.time.{ Instant, ZoneId, ZonedDateTime }
import java.util.Properties

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */
case object BuildInfo {

  private val props: Properties = {
    val props = new Properties
    Option(getClass.getClassLoader.getResourceAsStream("buildinfo.properties"))
      .foreach(props.load)
    props
  }

  private val buildProperties: Map[String, Option[String]] = {
    Map(
      "version" -> Option(props.getProperty("version")),
      "name" -> Option(props.getProperty("name")),
      "buildTime" -> Option(props.getProperty("buildTime")),
      "organization" -> Option(props.getProperty("organization"))
    )
  }

  def version: String = buildProperties("version").getOrElse("0.0.0")
  def projectName: String = buildProperties("name").getOrElse("unknown")
  def organization: String = buildProperties("organization").getOrElse("unknown")
  def buildTime: ZonedDateTime = {
    val utc = ZoneId.of("UTC")
    buildProperties("buildTime")
      .map { t => ZonedDateTime.ofInstant(Instant.ofEpochMilli(t.toLong), utc) }
      .getOrElse(ZonedDateTime.now().withZoneSameInstant(utc))
  }

  def getFrontendLibVersion(lib: String): Option[String] =
    Option(props.getProperty(s"frontend.$lib"))

}
