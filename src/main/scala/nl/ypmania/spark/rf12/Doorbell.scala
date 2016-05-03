package nl.ypmania.spark.rf12

import akka.actor.Actor
import com.typesafe.config.Config

class Doorbell(config: Config) extends Actor {
  val id1, id2 = config.getString("id").toList match {
    case c1 :: c2 :: Nil => (c1, c2)
    case _ => throw new IllegalArgumentException("id must have length 2")
  }
  
  override def receive = {
    case _ =>
      
  }
}