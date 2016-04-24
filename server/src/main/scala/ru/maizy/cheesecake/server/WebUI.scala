package ru.maizy.cheesecake.server

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */


import java.nio.file.Paths
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
      val npmWebjarsRoot = s"${webRoot.toString}/node-modules/main/$WEBJARS"
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
        // TODO: is there any way to have the same structure as in the final assembled jar?
        val frontendLibs = Map(
          "immutable" -> BuildInfo.getFrontendLibVersion("immutable").get,
          "react" -> BuildInfo.getFrontendLibVersion("react").get,
          "react-dom" -> BuildInfo.getFrontendLibVersion("react").get,
          "bootstrap" -> BuildInfo.getFrontendLibVersion("bootstrap").get
        )

        frontendLibs.collect { case(lib, version) =>
          pathPrefix(lib / version) {
            encodeResponse {
              get(getFromDirectory(s"$npmWebjarsRoot/$lib"))
            }
          }
        }.reduce((a, b) => a ~ b)
      }
    } else {
      val projectWebjar = s"$WEBJARS/${BuildInfo.projectName}/$Version"
      pathSingleSlash {
        encodeResponse {
          get(getFromResource(s"$projectWebjar/index.html"))
        }
      } ~
      pathPrefix(ASSETS) {
        encodeResponse {
          get(getFromResourceDirectory(s"$projectWebjar"))
        }
      } ~
      pathPrefix(LIBS) {
        encodeResponse {
          get(getFromResourceDirectory(WEBJARS))
        }
      }
    }
  }

  val routes: Route = staticRoutes
}
