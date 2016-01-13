name := "mongochitchat"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  "org.reactivemongo" %% "play2-reactivemongo" % "0.10.2"
)     

play.Project.playScalaSettings

// The ReactiveMongo repository
resolvers += "Sonatype Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"
