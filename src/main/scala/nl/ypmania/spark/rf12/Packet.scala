package nl.ypmania.spark.rf12

import nl.ypmania.spark.proxy.RadioMessage

object Packet {
  def apply(bytes: Seq[Byte]): Packet = Packet(bytes.head, bytes.tail)
}

case class Packet(header: Byte, contents: Seq[Byte]) extends RadioMessage