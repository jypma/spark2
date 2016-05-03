package nl.ypmania.spark.fs20

import akka.actor.Actor
import com.typesafe.config.Config
import scala.collection.JavaConverters._

class Dimmer (config: Config) extends Actor {
  val houseCode = HouseCode(config.getString("housecode"))
  val addresses = config.getStringList("addresses").asScala.map(Address.apply).toSeq
  
  override def receive = {
    case _ =>
      
  }
}