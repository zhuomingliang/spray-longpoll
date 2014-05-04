package com.leajoy.actors.comsumers

import java.nio.charset.Charset;
import akka.actor.{Actor, ActorLogging}
import kafka.consumer.KafkaStream;
import akka.event.Logging

class ConsumerStreamActor  extends Actor with ActorLogging {
	def receive = {
		case ConsumerMessageStream(message, transform, req) ⇒ {
		  log.debug("收到了一个主题的消息")
		  //stream.foreach(m => req.complete(new String(m.message, Charset.forName("UTF-8")))
		}
		
		case _ ⇒ {
		  log.info("Unexpected message recieved in ConsumerStreamActor")
		}
	}
}