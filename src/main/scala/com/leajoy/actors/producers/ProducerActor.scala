package com.leajoy.actors.producers

import java.nio.charset.Charset;
import akka.actor.{Actor, ActorLogging}
import kafka.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import com.leajoy.messages.TextMessage

class ProducerActor(config: ProducerConfig) extends Actor with ActorLogging {
	val producer = new Producer[String, String](config)

	def receive = {
		case TextMessage(topic, key, message) ⇒ {
		  log.debug("Recieved a topic : {}, key : {}, message: {}", topic, key, message)
		  
		  if ( key == null)
			  producer.send(new KeyedMessage[String, String](topic, message))
		  else
		      producer.send(new KeyedMessage[String, String](topic, key, message))
		}
		
		case _ ⇒ {
		  log.info("Unexpected message recieved in ProducerActor")
		}
	}
	
	// Ensure that the constructed ActorSystem is shut down when the JVM shuts down
	sys.addShutdownHook({
		log.info("Shutdown")
		producer.close
	})
	
}