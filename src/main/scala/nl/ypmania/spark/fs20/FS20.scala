package nl.ypmania.spark.fs20

import akka.util.ByteString
import nl.ypmania.spark.proxy.RadioMessage

case class Address private (byte: Int) extends AnyVal

object Address {
  def apply(a: String): Address = {
    var i = Integer.valueOf(a)
    var result = 0
    result += ((i / 1000) - 1) * 64
    i %= 1000
    result += ((i / 100) - 1) * 16
    i %= 100
    result += ((i / 10) - 1) * 4
    i %= 10
    result += (i - 1)
    new Address(result)    
  }
  
  implicit def apply(byte: Byte): Address = Address(byte)
}

case class HouseCode (hi: Address, lo: Address)

object HouseCode {
  def apply(s: String): HouseCode = if (s.length() == 8)
    new HouseCode(Address(s.substring(0, 4)), Address(s.substring(4, 8)))
  else
    throw new IllegalArgumentException ("House code must be length 8, and not: " + s)
}

case class Command (value: Int) {
	require(value >= 0 && value < 32)
}

object Command {
  val on = Command(16)
  val off = Command(0)
}

case class Packet(houseCode: HouseCode, address: Address, brightness: Command) extends RadioMessage
object Packet {
  def apply(bytes: ByteString): Packet = Packet(HouseCode(hi = bytes(0), lo = bytes(1)), Address(bytes(2)), Command(bytes(3) & 0x1F))
}
