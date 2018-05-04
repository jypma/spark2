package nl.ypmania.spark.fs20

import org.scalatest.WordSpec
import org.scalatest.Matchers
import FS20Actor.State
import FS20Actor.Actuator
import Command._

class FS20ActorStateSpec extends WordSpec with Matchers {
  val lights = Address("4444")
  
  val room1 = Address("4411")
  val room2 = Address("4412")
  
  val room1light1 = Actuator(Address("1113"), Set(room1, lights), zone = null)
  val room1light2 = Actuator(Address("1112"), Set(room1, lights), zone = null)
  val room2light1 = Actuator(Address("1111"), Set(room2, lights), zone = null)
  val houseCode = HouseCode("12341234")
  
  "FS20Actor.State" when {
    "having light 1 in room 1 turned on" should {
      val have = State(Map(
          room1light1 -> on, 
          room1light2 -> off,
          room2light1 -> off))
      
      "take no action if need is equal to have" in {
        val need = have
        need.planFrom(houseCode, have) shouldBe empty 
      }
          
      "turn new lights on before turning old lights off" in {
        val need = State(Map(
          room1light1 -> off, 
          room1light2 -> off,
          room2light1 -> on))
          
        need.planFrom(houseCode, have) should be(Seq(
            Packet(houseCode, room2light1.primaryAddress, on), 
            Packet(houseCode, room1light1.primaryAddress, off)))
      }
    }
    
    "having all lights in room 1 turned on but on different brightness" should {
      val have = State(Map(
          room1light1 -> on, 
          room1light2 -> Command(3),
          room2light1 -> off))
     
      "turn room 1 off with one command" in {
        val need = State(Map(
          room1light1 -> off, 
          room1light2 -> off,
          room2light1 -> off))
          
        need.planFrom(houseCode, have) should be (Seq(Packet(houseCode, room1, off)))
      }
    }
  }
}