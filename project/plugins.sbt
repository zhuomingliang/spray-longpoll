addSbtPlugin("io.spray" % "sbt-revolver" % "0.7.2")

resolvers += "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"

// needed because sbt-twirl depends on twirl-compiler which is only available
// at repo.spray.io
resolvers += "spray repo" at "http://repo.spray.io"

addSbtPlugin("io.spray" % "sbt-twirl" % "0.7.0")

addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "2.5.0")
