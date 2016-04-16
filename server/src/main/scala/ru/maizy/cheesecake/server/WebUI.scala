package ru.maizy.cheesecake.server

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */


import java.nio.file.{ Path, Paths }
import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route


class WebUI(system: ActorSystem) {

  val WEBJARS = "webjars"
  val ASSETS = "assets"
  val LIBS = "libs"

  private val staticRoutes: Route = {
    val devMode = System.getProperty("assetsMode") == "dev"
    if (devMode) {
      // TODO: is there any way to compile assets to the same dir as in an assemblied jar?
      val webRoot = Paths.get("server/target/web").toAbsolutePath
      val compiledStatic = s"${webRoot.toString}/public/main"
      pathSingleSlash {
        encodeResponse {
          getFromFile(s"$compiledStatic/index.html")
        }
      } ~
      pathPrefix(ASSETS) {
        encodeResponse {
          get(getFromDirectory(compiledStatic))
        }
      } ~
      pathPrefix(LIBS) {
        encodeResponse {
          get(getFromDirectory(s"${webRoot.toString}/node-modules/main/$WEBJARS"))
        }
      }
    } else {
      val projectWebjar = s"$WEBJARS/${BuildInfo.projectName}/$Version"
      pathSingleSlash {
        encodeResponse {
          getFromResource(s"$projectWebjar/index.html")
        }
      } ~
      pathPrefix(ASSETS) {
        encodeResponse {
          get(getFromResourceDirectory(s"$projectWebjar"))
        }
      } ~
      // TODO: find better solution
      pathPrefix(s"$LIBS/immutable") {
        encodeResponse {
          get(getFromResourceDirectory(s"$WEBJARS/immutable/3.7.5"))
        }
      }
    }
  }

  val routes: Route = staticRoutes
}
