package nl.ypmania.spark.fs20

import akka.actor.ActorRef
case class Address private (value: Int) extends AnyVal

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
}

case class HouseCode (lo: Address, hi: Address)

object HouseCode {
  def apply(s: String) = if (s.length() == 8)
    new HouseCode(Address(s.substring(0, 4)), Address(s.substring(4, 8)))
  else
    throw new IllegalArgumentException ("House code must be length 8, and not: " + s)
}

case class Actuator(primaryAddress: Address, otherAddresses: Set[Address], zone: ActorRef) {
  def respondsTo(address: Address) = primaryAddress == address || otherAddresses(address)
}

case class Brightness (value: Int) {
	require(value >= 0 && value <= 16)
}

object Brightness {
  val on = Brightness(16)
  val off = Brightness(0)
}

case class Command(address: Address, brightness: Brightness)
