package com.leajoy.actors.comsumers

import java.util.Properties
import akka.actor.{Actor, ActorLogging, Props}
import akka.event.Logging
import kafka.consumer.Consumer
import kafka.consumer.ConsumerConfig
import kafka.consumer.KafkaStream
import spray.routing.RequestContext
import com.leajoy.transforms.Transform

case class ConsumerMessage(val stream: KafkaStream[Array[Byte],Array[Byte]], val transform: Transform)
case class CometConsumer (val transform: Transform)

class ConsumerActor(topics: List[String], config: ConsumerConfig) extends Actor with ActorLogging {
	private val consumer = Consumer.create(config)
	private var topicCount : Map[String, Int] = Map.empty
	
    // create 1 partitions of the stream for topic , to allow 1 threads to consume  
    topics.foreach(topic => topicCount += (topic -> 1))
    private val topicMessageStreams  = consumer.createMessageStreams(topicCount)

	private val MessageStreamsActor = context.actorOf(Props[ConumerMessageStreamsActor], name = "ConumerMessageStreamsActor")
	
	def receive = {
		case CometConsumer(transform) ⇒ {
		  log.info("收到了所有的主题")
		  
		  log.debug("Send all topics's messages")
		  topicMessageStreams.foreach( topicMessage => {
		    topicMessage._2.foreach( stream => {
		    	MessageStreamsActor  !  ConsumerMessage(stream, transform)
		    })
		  })
		  
		  consumer.commitOffsets
		  //consumer.shutdown
		}
		
		case _ ⇒ {
		  log.info("ConsumerActor 收到了不期待的数据")
		}
	}
	
	// Ensure that the constructed ActorSystem is shut down when the JVM shuts down
	sys.addShutdownHook({
		log.info("Shutdown")
		
		// 暂时无法确定是否需要提交
	    // connector.commitOffsets
		consumer.shutdown
		context.stop(MessageStreamsActor)
	})
}