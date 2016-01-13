name := "chitchat"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  javaCore,
  "playlastik" % "playlastik_2.10" % "0.90.10.3"
  //"playlastik"  % "playlastik_2.10" % "1.2.1.2-SNAPSHOT"
)     

play.Project.playScalaSettings

// The PlayLastik repository
resolvers += Resolver.url("Fred's GitHub Play Repository", url("http://fmasion.github.com/releases/"))(Resolver.ivyStylePatterns)
