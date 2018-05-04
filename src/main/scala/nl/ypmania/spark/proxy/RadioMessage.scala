package nl.ypmania.spark.proxy

import akka.util.ByteString
import nl.ypmania.spark.fs20
import nl.ypmania.spark.rf12
import nl.ypmania.spark.visonic

object RadioMessage {
  def apply(data: ByteString): Option[RadioMessage] = data match {
    case ByteString.empty => None
    case 'F' +: bytes if bytes.length >= 5 => Some(fs20.Packet(bytes))
    case 'R' +: bytes if bytes.length >= 1 => Some(rf12.Packet(bytes))
    case 'V' +: bytes if bytes.length >= 5 => Some(visonic.Packet(bytes))
    case 'P' +: bytes if bytes.length >= 17 => Some(Ping(bytes.utf8String))
    case _ => None
  }
}

trait RadioMessage {
  
}

case class Ping(id: String) extends RadioMessage