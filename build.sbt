name := "cheesecake"
organization := "ru.maizy"
version := "0.0.1"
scalaVersion := "2.11.7"

lazy val api = project.in(file("api"))

lazy val server = project.in(file("server")).dependsOn(api)
