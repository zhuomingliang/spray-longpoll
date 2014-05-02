package com.leajoy.messages

import spray.json._
import DefaultJsonProtocol._ // !!! IMPORTANT, else `convertTo` and `toJson` won't work correctly
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

case class TextMessage(val topic : String, val key : String, val message : String)  {
  	// val LOG = LoggerFactory.getLogger(classOf[TextMessage])

    def this(topic : String, message : String) = this(topic, null, message)
    
    def encode() : JsValue = {
  	    if(key == null)
  	      ("topic" -> topic, "message" -> message).toJson
  		else
  		  ("topic" -> topic, "key" -> key, "message" -> message).toJson
  	}
}

object TextMessageJsonProtocol extends DefaultJsonProtocol {
	implicit val TextMessageFormat = jsonFormat3(TextMessage.apply)
}

object TextMessage {
	import TextMessageJsonProtocol._
    def decode(str : String) : TextMessage = str.parseJson.convertTo[TextMessage]
}
