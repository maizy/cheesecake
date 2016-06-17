name := "cheesecake"

lazy val commonSettings = Seq(
  organization := "ru.maizy",
  version := "0.0.3",
  scalaVersion := "2.11.8",
  maintainer := "Nikita Kovaliov <nikita@maizy.ru>",
  packageSummary := "service monitoring tool",
  scalacOptions ++= Seq(
    "-target:jvm-1.8",
    "-encoding", "UTF-8",
    "-deprecation",
    "-unchecked",
    "-explaintypes",
    "-Xfatal-warnings",
    "-Xlint:_",
    "-Ywarn-dead-code",
    "-Ywarn-inaccessible",
    "-Ywarn-infer-any",
    "-Ywarn-nullary-override",
    "-Ywarn-nullary-unit",
    "-Ywarn-numeric-widen",
    "-Ywarn-unused",
    "-Ywarn-unused-import"
  )
)

lazy val commonDependencies = Seq(
  libraryDependencies ++= Seq(
    "com.typesafe" % "config" % "1.3.0",
    "org.scalatest" %% "scalatest" % "2.2.6" % "test"
  )
)

lazy val core = project
  .in(file("core"))
  .settings(commonDependencies: _*)
  .settings(commonSettings: _*)

lazy val api = project
  .in(file("api"))
  .settings(commonSettings: _*)

lazy val client = project
  .in(file("client"))
  .settings(commonDependencies: _*)
  .settings(commonSettings: _*)
  .dependsOn(core)
  .dependsOn(api)

lazy val server = project
  .in(file("server"))
  .settings(commonDependencies: _*)
  .settings(commonSettings: _*)
  .dependsOn(core)
  .dependsOn(api)
