import sbt._
import Keys._

class ShowgregatorServiceBuild extends Build {
  lazy val initTables = taskKey[Unit]("initTables")
  initTables <<= runTask(Compile, "org.showgregator.service.InitTables")
}