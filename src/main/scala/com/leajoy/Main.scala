package com.leajoy

import akka.actor.{ActorSystem, Props}
import akka.event.Logging
import akka.io.IO
import spray.can.Http
import com.leajoy.configurations.{SiteConfiguration, SiteSslConfiguration}
import com.leajoy.actors.comet.CometServiceActor

object Main extends App with SiteSslConfiguration {
  implicit val system = ActorSystem("site")
  val log = Logging(system, getClass)
  val configuration = SiteConfiguration(system)

  // println(root.render(new StringRendering).get)
  // system.shutdown()
  log.info("Starting service actor and HTTP server...")
  val service = system.actorOf(Props(new CometServiceActor(configuration)), "comet-service")
  IO(Http) ! Http.Bind(service, configuration.interface, configuration.port)
  
  // Ensure that the constructed ActorSystem is shut down when the JVM shuts down
  sys.addShutdownHook({
      log.info("Shutdown")
	  system.shutdown()
   })
}