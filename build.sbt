import sbt.Keys.libraryDependencies

enablePlugins(CrossPerProjectPlugin)

lazy val scalaSettings = Seq(
  scalaVersion := "2.11.8",
  scalacOptions += "-deprecation",
  scalacOptions += "-unchecked",
  scalacOptions += "-feature",
  crossScalaVersions := Seq("2.11.8", "2.12.1"),
  libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % "3.0.1" % "test"
  )
)

lazy val javaSettings = Seq(
  crossPaths := false,
  autoScalaLibrary := false,
  crossScalaVersions := Seq("2.11.8") // it's not really used; it's just about turning-off the crosscompilation
)

lazy val Versions = new {
  val dropwizard = "3.2.2"
  val typesafeConfig = "1.3.1"
  val grpc = "1.10.0"
  val slf4j = "1.7.25"
  val assertj = "3.8.0"
}

lazy val commonSettings = Seq(
  organization := "com.avast.metrics",
  version := sys.env.getOrElse("TRAVIS_TAG", "0.1-SNAPSHOT"),
  description := "Library for application monitoring",

  licenses ++= Seq("MIT" -> url(s"https://github.com/avast/metrics/blob/${version.value}/LICENSE")),
  publishArtifact in Test := false,
  bintrayOrganization := Some("avast"),
  bintrayPackage := "metrics",
  pomExtra := (
    <scm>
      <url>git@github.com:avast/metrics.git</url>
      <connection>scm:git:git@github.com:avast/metrics.git</connection>
    </scm>
      <developers>
        <developer>
          <id>avast</id>
          <name>Jakub Janecek, Avast Software s.r.o.</name>
          <url>https://www.avast.com</url>
        </developer>
      </developers>
    ),
  libraryDependencies ++= Seq(
    "org.mockito" % "mockito-all" % "1.10.19" % "test",
    "junit" % "junit" % "4.12" % "test",
    "com.novocode" % "junit-interface" % "0.10" % "test", // Required by sbt to execute JUnit tests
    "ch.qos.logback" % "logback-classic" % "1.1.8" % "test"
  ),
  testOptions += Tests.Argument(TestFrameworks.JUnit)
)

lazy val root = (project in file("."))
  .settings(
    name := "metrics",
    publish := {},
    publishLocal := {}
  ).aggregate(api, scalaApi, core, dropwizardCommon, jmx, jmxAvast, graphite, filter, formatting, statsd, grpc)

lazy val api = (project in file("api")).
  settings(
    commonSettings,
    javaSettings,
    name := "metrics-api"
  )

lazy val scalaApi = (project in file("scala-api")).
  settings(
    commonSettings,
    scalaSettings,
    name := "metrics-scala"
  ).dependsOn(api, jmx % "test")

lazy val core = (project in file("core")).
  settings(
    commonSettings,
    javaSettings,
    name := "metrics-core",
    libraryDependencies ++= Seq(
      "org.slf4j" % "slf4j-api" % Versions.slf4j,
      "org.assertj" % "assertj-core" % Versions.assertj % "test"
    )
  ).dependsOn(api)

lazy val dropwizardCommon = (project in file("dropwizard-common")).
  settings(
    commonSettings,
    javaSettings,
    name := "metrics-dropwizard-common",
    libraryDependencies ++= Seq(
      "io.dropwizard.metrics" % "metrics-core" % Versions.dropwizard,
      "org.slf4j" % "slf4j-api" % Versions.slf4j
    )
  ).dependsOn(core)

lazy val jmx = (project in file("jmx")).
  settings(
    commonSettings,
    javaSettings,
    name := "metrics-jmx"
  ).dependsOn(dropwizardCommon)

lazy val jmxAvast = (project in file("jmx-avast")).
  settings(
    commonSettings,
    javaSettings,
    name := "metrics-jmx-avast"
  ).dependsOn(jmx)

lazy val graphite = (project in file("graphite")).
  settings(
    commonSettings,
    javaSettings,
    name := "metrics-graphite",
    libraryDependencies ++= Seq(
      "io.dropwizard.metrics" % "metrics-graphite" % Versions.dropwizard
    )
  ).dependsOn(dropwizardCommon)

lazy val filter = (project in file("filter")).
  settings(
    commonSettings,
    javaSettings,
    name := "metrics-filter",
    libraryDependencies ++= Seq(
      "com.typesafe" % "config" % Versions.typesafeConfig
    )
  ).dependsOn(dropwizardCommon)

lazy val grpc = (project in file("grpc")).
  settings(
    commonSettings,
    javaSettings,
    name := "metrics-grpc",
    libraryDependencies ++= Seq(
      "io.grpc" % "grpc-core" % Versions.grpc,
      "io.grpc" % "grpc-protobuf" % Versions.grpc % "test",
      "io.grpc" % "grpc-stub" % Versions.grpc % "test",
      "io.grpc" % "grpc-services" % Versions.grpc % "test",
      "com.google.protobuf" % "protobuf-java" % "3.5.0" % "test"
    )
  ).dependsOn(core)

lazy val formatting = (project in file("formatting")).
  settings(
    commonSettings,
    javaSettings,
    name := "metrics-formatting",
    libraryDependencies ++= Seq(
      "com.typesafe" % "config" % Versions.typesafeConfig
    )
  ).dependsOn(dropwizardCommon, filter)

lazy val statsd = (project in file("statsd")).
  settings(
    commonSettings,
    javaSettings,
    name := "metrics-statsd",
    libraryDependencies ++= Seq(
      "com.datadoghq" % "java-dogstatsd-client" % "2.3",
      "org.slf4j" % "slf4j-api" % "1.7.22"
    )
  ).dependsOn(core, filter)
