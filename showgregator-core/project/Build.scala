import sbt._
import Keys._

class ShowgregatorCoreBuild extends Build {
  lazy val root = project.in(file("."))
    .settings(
      organization := "org.showgregator",
      name := "showgregator-core",
      version := "0.0.1",
      scalaVersion := "2.10.4",
      projectDependencies += "joda-time" %% "joda-time" % "2.6",
      projectDependencies += "org.scalacheck" %% "scalacheck" % "1.12.1",
      projectDependencies += "org.scalatest" %% "scalatest" % "2.2.1"
  )
}