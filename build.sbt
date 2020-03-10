name := """LineDrop-Sample"""
organization := "io.linedrop"

version := "0.1"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

maintainer := "code@linedrop.io"

scalaVersion := "2.13.1"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.3" % Test

// https://mongodb.github.io/mongo-scala-driver/
libraryDependencies += "org.mongodb.scala" %% "mongo-scala-driver" % "2.8.0"

// https://github.com/t3hnar/scala-bcrypt
libraryDependencies += "com.github.t3hnar" %% "scala-bcrypt" % "4.1"

// SendGrid API
// https://github.com/sendgrid/sendgrid-java
libraryDependencies += "com.sendgrid" % "sendgrid-java" % "4.0.1"

