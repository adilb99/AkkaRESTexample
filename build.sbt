name := "untitled"

version := "0.1"

scalaVersion := "2.12.6"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.5.13",
  "com.typesafe.akka" %% "akka-stream" % "2.5.13",
  "com.typesafe.akka" %% "akka-http" % "10.1.3",
  "com.typesafe.akka" %% "akka-http-core"  % "10.1.3",
  "com.typesafe.play" %% "play-ws-standalone-json"       % "1.1.8",
  "com.typesafe.akka" %% "akka-slf4j"      % "2.5.13",
  "ch.qos.logback"    %  "logback-classic" % "1.2.3",
  "de.heikoseeberger" %% "akka-http-play-json"   % "1.17.0",
  
)