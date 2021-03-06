
organization  := "com.example"

version       := "0.1"

scalaVersion  := "2.10.4"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

resolvers ++= Seq(
  "spray repo" at "http://repo.spray.io/"
)

libraryDependencies ++= Seq(
  "io.spray"            %   "spray-can"     % "1.3.1",
  "io.spray"            %   "spray-routing" % "1.3.1",
  "io.spray"            %   "spray-testkit" % "1.3.1",
  "io.spray"            %%  "spray-json"    % "1.2.6",
  "com.typesafe.akka"   %%  "akka-actor"    % "2.3.2",
 // "org.slf4j"           %   "slf4j-simple"  % "1.7.7",
  "log4j"               %   "log4j"         % "1.2.17",
  "com.101tec"          %   "zkclient"      % "0.4",
  "com.yammer.metrics"  %   "metrics-core"  % "2.2.0",
  "org.specs2"          %%  "specs2"        % "1.13" % "test"
)

seq(Revolver.settings: _*)

seq(Twirl.settings: _*)
