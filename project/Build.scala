import sbt._
import Keys._

object ShowgregatorBuild extends Build {
  lazy val commonSettings = Seq(
    organization := "org.showgregator",
    version := "0.0.1",
    scalaVersion := "2.10.4",
    conflictManager := ConflictManager.strict
  )

  lazy val showgregator = project.in(file("."))
    .aggregate(core, service)

  lazy val core = project.in(file("showgregator-core"))
    .settings(commonSettings: _*)


  lazy val service = project.in(file("showgregator-service"))
    .dependsOn(core)
    .settings(commonSettings: _*)
}