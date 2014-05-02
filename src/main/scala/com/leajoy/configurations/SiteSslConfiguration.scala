package com.leajoy.configurations

import java.security.{SecureRandom, KeyStore}
import javax.net.ssl.{KeyManagerFactory, SSLContext, TrustManagerFactory}
import com.typesafe.config.Config
import akka.actor.ActorSystem
import akka.event.Logging
import spray.io._
import spray.util.SettingsCompanion

case class SslConfiguration (
    keyStorePath: String,
    password: String,
    protocols: Array[String],
    ciphers: Array[String],
    clientAuth: String) {

  require(keyStorePath.nonEmpty, "keyStorePath must be non-empty")
  require(ciphers.nonEmpty,  "ciphers must be non-empty")
}

object SslConfiguration extends SettingsCompanion[SslConfiguration]("spray.can.server.ssl") {
  def fromSubConfig(config: Config) = apply(
    config getString "keyStorePath",
    config getString "password",
    config getString "protocols" split ',' toArray,
    config getString "ciphers" split ',' toArray,
    config getString "clientAuth")
}

// for SSL support (if enabled in application.conf)
trait SiteSslConfiguration {
  val _system = ActorSystem("site-ssl")
  val _log = Logging(_system, getClass)
  val _configuration = SslConfiguration(_system)
  
  // Ensure that the constructed ActorSystem is shut down when the JVM shuts down
  sys.addShutdownHook(_system.shutdown())
  
  // if there is no SSLContext in scope implicitly the HttpServer uses the default SSLContext,
  // since we want non-default settings in this example we make a custom SSLContext available here
  implicit def sslContext: SSLContext = {
    val keyStore = KeyStore.getInstance("jks")    
    val keyManagerFactory = KeyManagerFactory.getInstance("SunX509")    
    val trustManagerFactory = TrustManagerFactory.getInstance("SunX509")
    val context = SSLContext.getInstance("TLS")
    
    _log.info("Configuring TLS.");
    keyStore.load(getClass.getResourceAsStream(_configuration.keyStorePath), _configuration.password.toCharArray)
    keyManagerFactory.init(keyStore, _configuration.password.toCharArray)
    trustManagerFactory.init(keyStore)
    context.init(keyManagerFactory.getKeyManagers, trustManagerFactory.getTrustManagers, new SecureRandom)
    context
  }

  // if there is no ServerSSLEngineProvider in scope implicitly the HttpServer uses the default one,
  // since we want to explicitly enable cipher suites and protocols we make a custom ServerSSLEngineProvider
  // available here
  implicit def sslEngineProvider: ServerSSLEngineProvider = ServerSSLEngineProvider { engine =>
      engine.setEnabledProtocols(_configuration.protocols)
      engine.setEnabledCipherSuites(_configuration.ciphers)
      engine
  }
}