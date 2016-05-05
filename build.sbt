name := "cheesecake"

lazy val commonSettings = Seq(
  organization := "ru.maizy",
  version := "0.0.2",
  scalaVersion := "2.11.8",
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

lazy val api = project.
    in(file("api")).
    settings(commonSettings: _*)

lazy val server = project.
    in(file("server")).
    settings(commonSettings: _*).
    dependsOn(api)
