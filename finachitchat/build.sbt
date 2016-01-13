name := "finachitchat"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.10.3"

libraryDependencies ++= Seq(
  "com.twitter" %% "finatra" % "1.5.1",
  "com.sksamuel.elastic4s" %% "elastic4s" % "1.1.1.2"
)

libraryDependencies += "io.fastjson" % "boon" % "0.23"

resolvers += "Twitter" at "http://maven.twttr.com"
    