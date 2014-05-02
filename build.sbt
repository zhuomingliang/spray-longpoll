
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
//  "org.slf4j"           %   "slf4j-api"  % "1.7.7",
  "org.specs2"          %%  "specs2"        % "1.13" % "test"
)

seq(Revolver.settings: _*)

seq(Twirl.settings: _*)
