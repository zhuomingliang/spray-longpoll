package com.leajoy.actors.comet

import java.util.Properties;
import akka.actor._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import spray.routing._
import spray.http._
import spray.httpx.TwirlSupport._
import kafka.consumer.ConsumerConfig
import kafka.producer.ProducerConfig;
import com.leajoy.configurations.SiteConfiguration
import com.leajoy.actors.comsumers._
import com.leajoy.actors.producers.ProducerActor
import com.leajoy.messages.TextMessage
import com.leajoy.transforms.Transform

// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class CometServiceActor(val configuration: SiteConfiguration,   val producerConfig : Properties, val consumerConfig : Properties) extends Actor with CometService {

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling
  def receive = runRoute(myRoute)
  
  val cometActor = actorRefFactory.actorOf(Props(new CometActor(producerConfig,  consumerConfig)))
  
  val myRoute =
    path("") {
      get {
          complete(html.page())
      }
    } ~
    path("comet") {
      get {
        parameters('id) { id:String =>
          (cometActor ! Poll(id, _))
        }
      }
    } ~
    path("sendMessage") {
      get {
        parameters('name, 'message) { (name:String, message:String) =>
          cometActor ! ProducerMessage(name, message)
          cometActor ! BroadcastMessage(HttpEntity("%s : %s".format(name, message)))
          complete(StatusCodes.OK)
        }
      }
    } ~
    pathPrefix("static" / Segment) { dir =>
      getFromResourceDirectory(dir)
    }
}

/**
 * CometMessage used when some data needs to be sent to a client
 * with the given id
 */
case class CometMessage(id: String, data: HttpEntity, counter: Long = 0)

/**
 * BroadcastMessage used when some data needs to be sent to all
 * alive clients
 * @param data
 */
case class BroadcastMessage(data: HttpEntity)

case class ProducerMessage(key : String, message : String)

/**
 * Poll used when a client wants to register/long-poll with the
 * server
 */
case class Poll(id: String, reqCtx: RequestContext)

/**
 * PollTimeout sent when a long-poll requests times out
 */
case class PollTimeout(id: String)

/**
 * ClientGc sent to deregister a client when it hasn't responded
 * in a long time
 */
case class ClientGc(id: String)

class CometActor(producerConfig: Properties, consumerConfig: Properties) extends Actor with ActorLogging  {
  private var aliveTimers: Map[String, Cancellable] = Map.empty   // list of timers that keep track of alive clients
  private var toTimers: Map[String, Cancellable] = Map.empty      // list of timeout timers for clients
  private var requests: Map[String, RequestContext] = Map.empty   // list of long-poll RequestContexts

  private val gcTime = 1 minute               // if client doesn't respond within this time, its garbage collected
  private val clientTimeout = 7 seconds       // long-poll requests are closed after this much time, clients reconnect after this
  private val rescheduleDuration = 5 seconds  // reschedule time for alive client which hasn't polled since last message
  private val retryCount = 10                 // number of reschedule retries before dropping the message

  private val prodcuerActor = context.actorOf(Props(new ProducerActor(new ProducerConfig(producerConfig))), "ProducerActor")
  // TODO: 修改在配置里面设置主题
  private val consumerActor = context.actorOf(Props(new ConsumerActor(List("test"), new ConsumerConfig(consumerConfig))), "ConsumerActor")
  consumerActor ! CometConsumer(new Transform)
  
  def receive = {
    case Poll(id, reqCtx) =>
      requests += (id -> reqCtx)
      toTimers.get(id).map(_.cancel())
      toTimers += (id -> context.system.scheduler.scheduleOnce(clientTimeout, self, PollTimeout(id)))

      aliveTimers.get(id).map(_.cancel())
      aliveTimers += (id -> context.system.scheduler.scheduleOnce(gcTime, self, ClientGc(id)))

    case PollTimeout(id) =>
      requests.get(id).map(_.complete(HttpResponse(StatusCodes.OK)))
      requests -= id
      toTimers -= id

    case ClientGc(id) =>
      requests -= id
      toTimers -= id
      aliveTimers -= id

    case CometMessage(id, data, counter) =>
      val reqCtx = requests.get(id)
      reqCtx.map { reqCtx =>
        reqCtx.complete(data)
        requests = requests - id
      } getOrElse {
        if(aliveTimers.contains(id) && counter < retryCount) {
          context.system.scheduler.scheduleOnce(rescheduleDuration, self, CometMessage(id, data, counter+1))
        }
      }

    case BroadcastMessage(data) =>
      aliveTimers.keys.map { id =>
        //requests.get(id).map(_.complete(HttpResponse(entity=data))).getOrElse(self ! CometMessage(id, data))
      }
      toTimers.map(_._2.cancel)
      toTimers = Map.empty
      requests = Map.empty
      
    case ProducerMessage(key, data) =>
       // log.debug(producerConfig.getProperty("topic"))
       // TODO: 修改在配置里面设置主题
        log.debug("开始发送")
       prodcuerActor ! TextMessage("test", key, data)
  }

}

// this trait defines our service behavior independently from the service actor
trait CometService extends HttpService  {

}