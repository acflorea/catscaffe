name := """akka-sample-persistence-scala"""

version := "2.4.4"

scalaVersion := "2.11.7"

val akkaVersion = "2.4.4"

libraryDependencies ++= Seq(
  "org.slf4j" % "slf4j-api" % "1.7.7",
  "ch.qos.logback" % "logback-core" % "1.1.2",
  "ch.qos.logback" % "logback-classic" % "1.1.2",

  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-persistence" % akkaVersion,
  "org.iq80.leveldb" % "leveldb" % "0.7",
  "org.fusesource.leveldbjni" % "leveldbjni-all" % "1.8",
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion
)

licenses := Seq(("CC0", url("http://creativecommons.org/publicdomain/zero/1.0")))

fork in run := true