import com.typesafe.sbt.packager.docker._
enablePlugins(JavaServerAppPackaging, SbtWeb, DockerPlugin)


name := "cheesecake-server"

val akkaVersion = "2.4.7"
val akkaStreamsVersion = akkaVersion

val immutableJsVersion = "3.8.1"
val reactVersion = "15.0.1"
val bootstrapVersion = "3.3.6"
val requireJsVersion = "2.2.0"
val humanizeDuration = "3.7.1"

libraryDependencies ++= Seq(
  "ch.qos.logback" % "logback-classic" % "1.1.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.4.0",
  "com.github.scopt" %% "scopt" % "3.5.0",
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaStreamsVersion,
  "com.typesafe.akka" %% "akka-http-core" % akkaStreamsVersion,
  "com.typesafe.akka" %% "akka-http-experimental" % akkaStreamsVersion,
  "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaStreamsVersion,
  "net.virtual-void" %%  "json-lenses" % "0.6.1",
  "com.nrinaudo" %% "kantan.xpath" % "0.1.4",
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test"
)


// Scalastyle setup
lazy val testScalastyle = taskKey[Unit]("testScalastyle")
lazy val testScalastyleInCompile = taskKey[Unit]("testScalastyleInCompile")
testScalastyle := org.scalastyle.sbt.ScalastylePlugin.scalastyle.in(Test).toTask("").value
testScalastyleInCompile := org.scalastyle.sbt.ScalastylePlugin.scalastyle.in(Compile).toTask("").value
(test in Test) <<= (test in Test) dependsOn (testScalastyle, testScalastyleInCompile)
scalastyleFailOnError := true

// Build info
resourceGenerators in Compile += Def.task {
  val file = (resourceManaged in Compile).value / "buildinfo.properties"
  val contents = Seq(
    s"version=${version.value}",
    s"name=${name.value}",
    s"organization=${organization.value}",
    s"buildTime=${System.currentTimeMillis()}",
    s"frontend.immutable=$immutableJsVersion",
    s"frontend.react=$reactVersion",
    s"frontend.bootstrap=$bootstrapVersion",
    s"frontend.requirejs=$requireJsVersion",
    s"frontend.humanizeduration=$humanizeDuration"
  ).mkString("\n")
  IO.write(file, contents)
  Seq(file)
}.taskValue

// Docker settings
val dockerRoot = "/opt/docker"
val dockerConfigs = "/configs"
val dockerUser = "daemon"
val dockerOwnership = s"$dockerUser:$dockerUser"

mappings in (Compile, packageDoc) := Seq()
version in Docker := version.value
dockerRepository := Some("maizy")

dockerCommands := Seq(
  Cmd("FROM", "anapsix/alpine-java:jre8"),
  Cmd("MAINTAINER", maintainer.value),

  ExecCmd("RUN", "mkdir", "-p", dockerRoot),
  ExecCmd("RUN", "chown", "-R", dockerOwnership, dockerRoot),

  ExecCmd("RUN", "mkdir", "-p", dockerConfigs),
  ExecCmd("RUN", "chown", "-R", dockerOwnership, dockerConfigs),

  Cmd("USER", dockerUser),
  Cmd("ADD", "opt", "/opt"),

  Cmd("EXPOSE", "52022"),
  Cmd("WORKDIR", dockerRoot),
  Cmd("VOLUME", dockerConfigs),

  ExecCmd("ENTRYPOINT", s"bin/${name.value}"),
  ExecCmd("CMD", "--host=0.0.0.0", s"--config=$dockerConfigs/cheesecake.conf")
)

// Frontend settings
JsEngineKeys.engineType := JsEngineKeys.EngineType.Node
libraryDependencies ++= Seq(
  "org.webjars.npm" % "requirejs" % requireJsVersion,
  "org.webjars.npm" % "immutable" % immutableJsVersion,
  "org.webjars.npm" % "react" % reactVersion,
  "org.webjars.npm" % "react-dom" % reactVersion,
  "org.webjars.npm" % "bootstrap" % bootstrapVersion,
  "org.webjars.npm" % "humanize-duration" % humanizeDuration
)
includeFilter in (Assets, LessKeys.less) := "*.page.less"
