package nl.ypmania.spark.zone

import akka.actor.Actor
import com.typesafe.config.Config
import scala.collection.JavaConverters._
import com.typesafe.config.ConfigObject
import com.typesafe.config.ConfigValueType
import com.typesafe.config.ConfigFactory
import akka.actor.Props
import Zone._
import nl.ypmania.spark.proxy.ProxyRegistry
import nl.ypmania.spark.proxy.RadioMessage

class Zone(config: Config) extends Actor {
	val proxies: Seq[String] = config.getStringList("proxies").asScala
	
  val DEFAULTS = ConfigFactory.parseString("""
    type: "main.Zone", 
    proxies: []
  """)
  
  for ((name, value) <- config.root.asScala 
       if value.valueType() == ConfigValueType.OBJECT;
       cfg = value.asInstanceOf[ConfigObject].toConfig.withFallback(DEFAULTS)) {
    val cls = getClass().getClassLoader().loadClass("nl.ypmania.spark." + cfg.getString("type"))
    context.actorOf(Props(cls, cfg), name)
  }
  
  override def receive = {
    case Transmit(msg) =>
      ProxyRegistry(context.system) ! ProxyRegistry.Transmit(proxies, msg)
  }
}

object Zone {
  case class Transmit(msg: RadioMessage)
}