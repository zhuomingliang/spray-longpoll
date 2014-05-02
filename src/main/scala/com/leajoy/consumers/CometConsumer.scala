package com.leajoy.consumers

import java.util.Properties;
import akka.actor.{ActorSystem, Props}
import akka.event.Logging
import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig; 
import kafka.consumer.KafkaStream;
import spray.routing.RequestContext
import com.leajoy.transforms.Transform
import com.leajoy.actors.comsumers.CometConsumerActor

case class ConsumerMessageList(topicMessageStreams: List[KafkaStream[Array[Byte],Array[Byte]]], transform: Transform, req: RequestContext)

case class CometConsumer (configProps : Properties, topics : List[String], transform: Transform, req: RequestContext) {
	val connector = Consumer.create(new ConsumerConfig(configProps))
	val system = ActorSystem("CometConsumer")
    val log = Logging(system, getClass)
	val consumer = system.actorOf(Props[CometConsumerActor], name = "CometConsumerActor")

	
	def start {
		var topicCount : Map[String, Int] = Map.empty
		topics.foreach(topic => topicCount += (topic -> 1))
		
		val topicMessageStreams  = connector.createMessageStreams(topicCount)
		
		log.debug("Send all topics's messages")
		topicMessageStreams.foreach( topicMessage => consumer  !  ConsumerMessageList(topicMessage._2, transform, req))
	}
	
	def stop {
		connector.commitOffsets
		connector.shutdown
	}
	
	// Ensure that the constructed ActorSystem is shut down when the JVM shuts down
	sys.addShutdownHook({
		log.info("Shutdown")
		
		// 暂时无法确定是否需要提交
	    // connector.commitOffsets
		
		connector.shutdown
		system.shutdown()
	})
}