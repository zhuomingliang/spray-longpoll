package com.leajoy.actors.comsumers

import akka.actor._
import kafka.consumer.KafkaStream;
import akka.event.Logging
import com.leajoy.consumers.ConsumerMessageList
import com.leajoy.transforms.Transform
import spray.routing.RequestContext

case class ConsumerMessageStream(stream: KafkaStream[Array[Byte],Array[Byte]], transform: Transform, req: RequestContext)

class CometConumerMessageStreamsTask(topicMessageStreams: List[KafkaStream[Array[Byte],Array[Byte]]], transform: Transform, req: RequestContext) {
	val system = ActorSystem("CometConsumerStream")
    val log = Logging(system, getClass)
	val streamActor = system.actorOf(Props[CometConsumerStreamActor], name = "CometConsumerStreamActor")
	
	def run {
		  log.debug("Send a topic's messages")
		  topicMessageStreams.foreach(stream => streamActor ! ConsumerMessageStream(stream, transform, req))
	}
	
	// Ensure that the constructed ActorSystem is shut down when the JVM shuts down
	sys.addShutdownHook({
		log.info("Shutdown")
		system.shutdown()
	})
}

class CometConsumerActor extends Actor with ActorLogging {
	def receive = {
		case ConsumerMessageList(topicMessageStreams, transform, req) ⇒ {
		  log.debug("Recieved a topic's messages")
		  (new CometConumerMessageStreamsTask(topicMessageStreams, transform, req)) run
		}
	}
}