name := "averageRestResponse"

version := "1.0"

scalaVersion := "2.11.1"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= {
  val akkaV = "2.3.6"
  val sprayV = "1.3.2"
  Seq(
    "io.spray"            %%  "spray-can"     % sprayV,
    "io.spray"            %%  "spray-routing" % sprayV,
    "io.spray" %% "spray-client" % sprayV,
    "io.spray"            %%  "spray-testkit" % sprayV  % "test",
    "com.typesafe.akka"   %%  "akka-actor"    % akkaV,
    "com.typesafe.akka"   %%  "akka-testkit"  % akkaV   % "test",
    "io.spray" %%  "spray-json" % "1.3.1",
    "org.specs2"          %%  "specs2-core"   % "2.3.11" % "test",
    "org.json4s" %% "json4s-jackson" % "3.2.11",
    "com.typesafe.play" %% "play-json" % "2.3.4"
  )
}