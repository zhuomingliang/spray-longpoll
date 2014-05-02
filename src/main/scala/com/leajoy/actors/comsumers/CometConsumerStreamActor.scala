package com.leajoy.actors.comsumers

import java.nio.charset.Charset;
import akka.actor._
import kafka.consumer.KafkaStream;
import akka.event.Logging

class CometConsumerStreamActor  extends Actor with ActorLogging {
	def receive = {
		case ConsumerMessageStream(stream, transform, req) ⇒ {
		  log.info("Recieved a topic's message ")
		  stream.foreach(m => req.complete(new String(m.message, Charset.forName("UTF-8"))))
		}
	}
}