name := "showgregator-core"

resolvers ++= Seq(
  "Open Source Geospatial Foundation Repository" at "http://download.osgeo.org/webdav/geotools/"
)

libraryDependencies += "org.scala-lang" % "scala-reflect" % "2.10.4"

projectDependencies += "joda-time" % "joda-time" % "2.6"

projectDependencies += "com.maxmind.geoip2" % "geoip2" % "2.1.0" exclude("com.google.code.findbugs", "jsr305")

libraryDependencies += "org.apache.lucene" % "lucene-core" % "4.10.3"

libraryDependencies += "org.apache.lucene" % "lucene-spatial" % "4.10.3"

libraryDependencies += "com.vividsolutions" % "jts" % "1.13"

libraryDependencies += "org.geotools" % "gt-shapefile" % "12.2"

projectDependencies += "org.scalacheck" %% "scalacheck" % "1.12.1" % "test"

projectDependencies += "org.scalatest" %% "scalatest" % "2.2.1" % "test"

dependencyOverrides += "commons-pool" % "commons-pool" % "1.5.4"