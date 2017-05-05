name := "akka-cluster"

version := "1.0"

scalaVersion := "2.12.1"

resolvers += Resolver.typesafeRepo("releases")

libraryDependencies ++= Seq(
  "org.specs2"                  %%  "specs2-core"       % "3.8.6"   % "test",
  "com.typesafe.akka"           %% "akka-actor"         % "2.4.17",
  "com.typesafe.akka"           %% "akka-cluster"       % "2.4.17",
  "com.typesafe.akka"           %% "akka-slf4j"         % "2.4.17",
  "ch.qos.logback"              % "logback-classic"     % "1.1.7"
)

scalacOptions in Test ++= Seq("-Yrangepos")