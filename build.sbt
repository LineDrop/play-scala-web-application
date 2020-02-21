name := """LineDrop-Sample"""
organization := "io.linedrop"

version := "0.1"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

maintainer := "code@linedrop.io"

scalaVersion := "2.12.0"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.3" % Test

// https://mongodb.github.io/mongo-scala-driver/
libraryDependencies += "org.mongodb.scala" %% "mongo-scala-driver" % "2.8.0" // works with scala 2.12 and 2.13

// https://github.com/playframework/play-mailer
libraryDependencies += "com.typesafe.play" %% "play-mailer" % "6.0.1" //  scala 2.13 not supported

// https://github.com/t3hnar/scala-bcrypt
libraryDependencies += "com.github.t3hnar" %% "scala-bcrypt" % "4.1"

