package ru.maizy.cheesecake.server

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import java.io.File

case class ServerAppOptions(
    port: Int = ServerAppOptions.DEFAULT_PORT,
    host: String = ServerAppOptions.DEFAULT_HOST,
    config: Option[File] = None
)

object ServerAppOptions {
  val DEFAULT_PORT = 52022
  val DEFAULT_HOST = "localhost"
}

object OptionParser {

  private val parser = new scopt.OptionParser[ServerAppOptions](s"java -jar ${BuildInfo.projectName}.jar") {

    head("Cheesecake server", Version.toString)
    help("help")
    version("version")
    opt[String]('h', "host")
      .text(s"host to listen on, default: ${ServerAppOptions.DEFAULT_HOST}")
      .action { (value, c) => c.copy(host = value) }
    opt[Int]('p', "port")
      .text(s"port to listen on, default: ${ServerAppOptions.DEFAULT_PORT}")
      .action { (value, c) => c.copy(port = value) }
    opt[File]('c', "config")
      .text("additional config")
      .valueName("<file>")
      .action { (value, c) => c.copy(config = Some(value)) }
  }

  def parse(args: Seq[String]): Option[ServerAppOptions] = {
    val opts = parser.parse(args, ServerAppOptions())
    val fails = Seq[Option[ServerAppOptions] => Boolean](
      _.isEmpty
    )
    if (fails.exists(_ (opts))) {
      parser.showUsageAsError
      None
    } else {
      opts
    }
  }
}
