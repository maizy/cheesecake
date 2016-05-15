enablePlugins(JavaServerAppPackaging, SbtWeb)

name := "cheesecake-server"

val akkaVersion = "2.4.4"
val akkaStreamsVersion = akkaVersion

val immutableJsVersion = "3.8.1"
val reactVersion = "15.0.1"
val bootstrapVersion = "3.3.6"
val requireJsVersion = "2.2.0"
val humanizeDuration = "3.7.1"

libraryDependencies ++= Seq(
  "ch.qos.logback" % "logback-classic" % "1.1.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.4.0",
  "com.github.scopt" %% "scopt" % "3.4.0",
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaStreamsVersion,
  "com.typesafe.akka" %% "akka-http-core" % akkaStreamsVersion,
  "com.typesafe.akka" %% "akka-http-experimental" % akkaStreamsVersion,
  "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaStreamsVersion,
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
