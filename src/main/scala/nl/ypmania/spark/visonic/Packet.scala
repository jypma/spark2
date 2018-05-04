package nl.ypmania.spark.visonic

import nl.ypmania.spark.proxy.RadioMessage

case class Packet (contents: Seq[Byte]) extends RadioMessage