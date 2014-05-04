package com.leajoy.actors.comsumers

import akka.actor.{Actor, ActorLogging, Props}
import kafka.consumer.KafkaStream;
import akka.event.Logging
import com.leajoy.transforms.Transform
import spray.routing.RequestContext
import java.nio.charset.Charset;

case class ConsumerMessageStream(val stream: KafkaStream[Array[Byte],Array[Byte]], val transform: Transform, val req: RequestContext)

class ConumerMessageStreamsActor extends Actor with ActorLogging {
	private val streamActor = context.actorOf(Props[ConsumerStreamActor], name = "ConsumerStreamActor") 
	
	def receive = {
		case ConsumerMessage(stream, transform) ⇒ {
		  log.debug("收到了一个主题的消息数组")
		  val it  = stream.iterator;
		  
		  while (it.hasNext) {
		    	log.debug("消息 {}", new String(it.next().message, Charset.forName("UTF-8")))
		    	// topicMessageStreams.foreach(stream => streamActor ! ConsumerMessageStream(transform, req))
		  }
		  log.debug("测试")
		}
		
		case _ ⇒ {
		  log.info("Unexpected message recieved in ConumerMessageStreamsActor")
		}
	}
	
	// Ensure that the constructed ActorSystem is shut down when the JVM shuts down
	sys.addShutdownHook({
		log.info("Shutdown")
		context.stop(streamActor)
	})
}