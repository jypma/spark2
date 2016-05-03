package nl.ypmania.spark.zone

import akka.actor.Actor
import com.typesafe.config.Config
import scala.collection.JavaConverters._
import com.typesafe.config.ConfigObject
import com.typesafe.config.ConfigValueType
import com.typesafe.config.ConfigFactory
import akka.actor.Props
import Zone._

class Zone(config: Config) extends Actor {
  val DEFAULTS = ConfigFactory.parseString("""
    type: "main.Zone" 
  """)
  
  for ((name, value) <- config.root.asScala 
       if value.valueType() == ConfigValueType.OBJECT;
       cfg = value.asInstanceOf[ConfigObject].toConfig.withFallback(DEFAULTS)) {
    val tst = DEFAULTS.getString("type")
    val cls = getClass().getClassLoader().loadClass("nl.ypmania.spark." + cfg.getString("type"))
    context.actorOf(Props(cls, cfg), name)
  }
  
  override def receive = {
    case SendToProxy(msg) =>
      // TODO send to proxy, configure proxy
  }
}

object Zone {
  case class SendToProxy(msg: Any)
}