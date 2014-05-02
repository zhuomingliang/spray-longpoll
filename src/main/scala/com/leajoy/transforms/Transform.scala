package com.leajoy.transforms

import com.leajoy.messages.TextMessage
import spray.routing.RequestContext

class Transform {
	def transform(message: TextMessage,  req: RequestContext) = {
	  message
	}
}