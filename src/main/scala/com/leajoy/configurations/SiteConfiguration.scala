package com.leajoy.configurations

import scala.collection.JavaConverters._
import com.typesafe.config.Config
import spray.util.SettingsCompanion

case class SiteConfiguration (
    interface: String,
    port: Int,
    devMode: Boolean,
    repoDirs: List[String],
    nightliesDir: String,
    mainVersion: String,
    otherVersions: Seq[String]) {

  require(interface.nonEmpty, "interface must be non-empty")
  require(0 < port && port < 65536, "illegal port")
}

object SiteConfiguration extends SettingsCompanion[SiteConfiguration]("spray.site") {
  def fromSubConfig(config: Config) = apply(
    config getString "interface",
    config getInt "port",
    config getBoolean "dev-mode",
    config getString "repo-dirs" split ':' toList,
    config getString "nightlies-dir",
    config getString "main-version",
    config getStringList("other-versions") asScala)
}