name := "showgregator-service"

Revolver.settings

mainClass in (Compile, run) := Some("org.showgregator.service.ShowgregatorServer")

fork := true

resolvers ++= Seq(
  "Typesafe repository snapshots"    at "http://repo.typesafe.com/typesafe/snapshots/",
  "Typesafe repository releases"     at "http://repo.typesafe.com/typesafe/releases/",
  "Sonatype repo"                    at "https://oss.sonatype.org/content/groups/scala-tools/",
  "Sonatype releases"                at "https://oss.sonatype.org/content/repositories/releases",
  "Sonatype snapshots"               at "https://oss.sonatype.org/content/repositories/snapshots",
  "Sonatype staging"                 at "http://oss.sonatype.org/content/repositories/staging",
  "Java.net Maven2 Repository"       at "http://download.java.net/maven/2/",
  "Twitter Repository"               at "http://maven.twttr.com",
  "Websudos releases"                at "http://maven.websudos.co.uk/ext-release-local"
)

projectDependencies += "com.datastax.cassandra" % "cassandra-driver-core" % "2.1.4"

projectDependencies += "joda-time" % "joda-time" % "2.6"

projectDependencies += "com.websudos" %% "phantom-dsl" % "1.5.0"

projectDependencies += "com.websudos" %% "phantom-zookeeper" % "1.5.0"

projectDependencies += "com.twitter" %% "finatra" % "1.6.0"

projectDependencies += "com.livestream" %% "scredis" % "2.0.6"

projectDependencies += "com.esotericsoftware" % "kryo" % "3.0.0"

projectDependencies += "com.websudos" %% "phantom-testing" % "1.5.0" % "test" exclude("com.twitter", "finagle_zookeeper") exclude("org.cassandraunit", "cassandra-unit")
//exclude("com.twitter.common.zookeeper", "server-set") exclude("com.twitter.common.zookeeper", "client") exclude("com.twitter.common.zookeeper", "group")
//excludeAll(
//  ExclusionRule(organization = "com.twitter", name = "finagle-zookeeper_2.10"),
//  ExclusionRule(organization = "org.cassandraunit", name = "cassandra-unit")
//  ),

projectDependencies += "org.cassandraunit" % "cassandra-unit" % "2.0.2.2" % "test"

projectDependencies += "junit" % "junit" % "4.12"

dependencyOverrides += "joda-time" % "joda-time" % "2.6"

dependencyOverrides += "com.datastax.cassandra" % "cassandra-driver-core" % "2.1.4"

dependencyOverrides += "com.google.guava" % "guava" % "16.0.1"

dependencyOverrides += "com.fasterxml.jackson.core" % "jackson-databind" % "2.3.1"

dependencyOverrides += "com.fasterxml.jackson.module" % "jackson-module-scala_2.10" % "2.3.1"

dependencyOverrides += "com.twitter" %% "finagle-core" % "6.20.0"

dependencyOverrides += "com.twitter" %% "util-core" % "6.19.0"

dependencyOverrides += "com.github.spullara.mustache.java" % "compiler" % "0.8.14"

dependencyOverrides += "io.netty" % "netty" % "3.9.4.Final"

dependencyOverrides += "com.twitter" %% "util-logging" % "6.19.0"

dependencyOverrides += "com.twitter" %% "util-collection" % "6.19.0"

dependencyOverrides += "com.twitter" %% "util-hashing" % "6.19.0"

dependencyOverrides += "org.scalatest" %% "scalatest" % "2.2.1" % "test"

dependencyOverrides += "commons-io" % "commons-io" % "2.1" // XXX watch out for this one, conflict with 1.3.2

dependencyOverrides += "org.apache.thrift" % "libthrift" % "0.5.0-1"

dependencyOverrides += "org.slf4j" % "slf4j-api" % "1.7.5"

dependencyOverrides += "com.google.code.findbugs" % "jsr305" % "2.0.1" // XXX vs 1.3.9

dependencyOverrides += "commons-lang" % "commons-lang" % "2.6"

dependencyOverrides += "junit" % "junit" % "4.12" // XXX vs 3.

dependencyOverrides += "com.typesafe.akka" %% "akka-actor" % "2.3.3"

dependencyOverrides += "com.typesafe" % "config" % "1.2.1"

dependencyOverrides += "org.xerial.snappy" % "snappy-java" % "1.1.1.3"

dependencyOverrides += "commons-codec" % "commons-codec" % "1.6"

dependencyOverrides += "jline" % "jline" % "1.0"

dependencyOverrides += "log4j" % "log4j" % "1.2.16"