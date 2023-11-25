
ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.12"

lazy val root = (project in file("."))
  .settings(
    name := "Policeman_Thief_Graph_Game"
  )

//test / fork := true
run / fork := true
run / connectInput := true

Compile / mainClass := Some("com.example.policemanthiefgame.Main")

libraryDependencies ++= Seq(
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.akka" %% "akka-http" % "10.2.10",
  "com.typesafe.akka" %% "akka-actor-typed" % "2.6.20",
  "com.typesafe.akka" %% "akka-stream" % "2.6.20",
  "com.google.guava" % "guava" % "31.0.1-jre",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "org.slf4j" % "slf4j-api" % "1.7.25",
  "org.scalatest" %% "scalatest" % "3.2.9" % Test,
  "com.typesafe.akka" %% "akka-testkit" % "2.6.20" % Test,
  "com.typesafe.akka" %% "akka-http-testkit" % "10.2.10" % Test,
  "org.scalaj" %% "scalaj-http" % "2.4.2",
  "software.amazon.awssdk" % "s3" % "2.16.83"
  // Other necessary libraries
)

val jarName = "police_thief_game.jar"
assembly / assemblyJarName := jarName

assembly / assemblyShadeRules := Seq(
  ShadeRule.rename("com.fasterxml.jackson.**" -> "shaded.jackson.@1").inAll
)
// In your build.sbt file
mainClass in (Compile, run) := Some("com.example.policemanthiefgame.Main")
// Merging strategies
ThisBuild / assemblyMergeStrategy := {
  case PathList("META-INF", _*) => MergeStrategy.discard
  case "reference.conf" => MergeStrategy.concat
  case _ => MergeStrategy.first
}