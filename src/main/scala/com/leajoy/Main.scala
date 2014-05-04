package com.leajoy

import java.io.FileInputStream;
import java.util.Properties;
import akka.actor.{ActorSystem, Props}
import akka.event.Logging
import akka.io.IO
import spray.can.Http
import org.apache.log4j.PropertyConfigurator;
import com.leajoy.configurations.{SiteConfiguration, SiteSslConfiguration}
import com.leajoy.actors.comet.CometServiceActor

object Main extends App with SiteSslConfiguration {
  implicit val system = ActorSystem("site")
  private val log = Logging(system, getClass)
  private val configuration = SiteConfiguration(system)
  private final val CONSUMER_PROPS_PATH : String  = "conf/consumer.properties";
  private final val PRODUCER_PROPS_PATH : String  = "conf/producer.properties";
  private final val LOG4J_PROPS_PATH : String  = "conf/log4j.properties";

  private final val  producerProps : Properties  = loadPropsFromFile(PRODUCER_PROPS_PATH)
  private final val  consumerProps : Properties = loadPropsFromFile(CONSUMER_PROPS_PATH)

  // println(root.render(new StringRendering).get)
  // system.shutdown()
  PropertyConfigurator.configure(LOG4J_PROPS_PATH);
  
  log.info("Starting service actor and HTTP server...")
  private val service = system.actorOf(Props(new CometServiceActor(configuration, producerProps,  consumerProps)), "comet-service")
    
  
  IO(Http) ! Http.Bind(service, configuration.interface, configuration.port)
  
  // Ensure that the constructed ActorSystem is shut down when the JVM shuts down
  sys.addShutdownHook({
      log.info("Shutdown")
	  system.shutdown
   })
   
   
    //@
    private def loadPropsFromFile(filename: String): Properties =
       try {
            val props = new Properties();
            props.load(new FileInputStream(filename));
            props
        } catch {
          case e: java.io.IOException =>
            log.error("Failed to load properties from file {}, exiting: {}", filename, e.getMessage());
            System.exit(-1);
            new Properties() // makes compiler happy
        }
}