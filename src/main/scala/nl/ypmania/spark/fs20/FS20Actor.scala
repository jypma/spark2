package nl.ypmania.spark.fs20

import akka.actor.Actor
import scala.concurrent.duration._
import akka.actor.ActorRef
import nl.ypmania.spark.zone.Zone

class FS20Actor extends Actor {
  import FS20Actor._
  
  private var have = State(Map.empty)
  private var want = State(Map.empty)
  private var plan = Seq.empty[Packet]
  private var zoneForAddress = Map.empty[Address,ActorRef]
  private val houseCode = HouseCode(self.path.name)
  
  override def receive = {
    case actuator: Actuator =>
      have += actuator -> Command.off
      zoneForAddress = have.actuators.keySet.map(a => a.otherAddresses.map(_ -> a.zone) + (a.primaryAddress -> a.zone)).flatten.toMap
    
    case Packet(h, address, brightness) if h == houseCode =>
      var needReplan = false
      for (actuator <- have.lookup(address)) {
        have += actuator -> brightness
        zoneForAddress = have.actuators.keySet.map(a => a.otherAddresses.map(_ -> a.zone) + (a.primaryAddress -> a.zone)).flatten.toMap
        for (b <- want.get(actuator) if b != brightness) {
          needReplan = true
        }        
      }
      if (needReplan) {
        replan()
      }
      
    case Want(address, brightness) =>
      for (actuator <- have.lookupPrimary(address)) {
        want += actuator -> brightness
        for (b <- have.get(actuator) if b != brightness) {
          replan()
        }        
      }
      
    case Proceed if plan.isEmpty =>
      sender ! Idle
      
    case Proceed =>
      val command = plan.head
      zoneForAddress(command.address) ! Zone.Transmit(command)
      plan = plan.tail
      sender ! (if (plan.isEmpty) Idle else WantToProceed)
  }
  
  def replan() {
	  plan = want.planFrom(houseCode, have)
	  sender ! (if (plan.isEmpty) Idle else WantToProceed)
  }
}

object FS20Actor {
  private[fs20] case class State(actuators: Map[Actuator, Command]) {
    def + (t: (Actuator, Command)) = State(actuators + t)
    
    def lookup(address: Address): Iterable[Actuator] = actuators.filter { case (a,c) => a.respondsTo(address) }.map(_._1)
    
    def lookupPrimary (address: Address): Option[Actuator] = actuators.find { case (a,c) => a.primaryAddress == address }.map(_._1)
    
    def changesFrom (have: State): Map[Command, Set[Actuator]] = {
      val diff = for (
        (a, b) <- actuators;
        haveIt = have.get(a)
        if haveIt.isDefined && haveIt.get != b
      ) yield (a, b)
      diff.groupBy(_._2).mapValues(_.keys.toSet)
    }
    
    def get(a: Actuator) = actuators.get(a)
    
    def getBrightnesses(address: Address): Set[Command] = 
      actuators.filterKeys(_.respondsTo(address)).values.toSet
      
    def planFrom (houseCode: HouseCode, have: State): Seq[Packet] = {
      val plan = Seq.newBuilder[Packet]
      
  	  // find actuators that share brightness and a common code (that's not shared by other actuators)
      for ((brightness, actuators) <- changesFrom(have)) {
        // TODO consider multimap here
        val codes = scala.collection.mutable.Map.empty[Address,Set[Actuator]].withDefaultValue(Set.empty)
        
        // find all shared codes 
        for (actuator <- actuators; address <- actuator.otherAddresses) { 
          codes(address) = codes(address) + actuator 
        }
        
        for ((code, actuators) <- codes) {
      	  // only interested in codes that have size >= 2
      	  // remove codes that map to another actuator in "want" with a different brightness
          if (actuators.size < 2 || getBrightnesses(code) != Set(brightness)) {
            codes.remove(code)
          }
        }
        
        var remaining = actuators
        // sort candidate codes by number of target actuators. Then, pick from that list first, rinse and repeat.
        while (!codes.isEmpty) {
          val (nibble, actuators) = codes.maxBy(_._2.size)
          plan += Packet(houseCode, nibble, brightness)
          remaining --= actuators
          codes -= nibble
          for (a <- actuators) {
            // remove actuators from code candidates
            for ((nibble, actuators) <- codes) {
              val newActuators = actuators - a
              if (newActuators.isEmpty) {
                codes -= nibble
              } else {
                codes(nibble) = newActuators
              }
            }
          }
        }
        
        // The remaining actuators have to be applied one by one, since they don't share a code
        for (a <- remaining) {
          plan += Packet(houseCode, a.primaryAddress, brightness)
        }
      }
      
      plan.result()
    }
  }
  

  
  // requests:
  /** Sent by an actuator in order to register itself. No reply */
  case class Actuator(primaryAddress: Address, otherAddresses: Set[Address], zone: ActorRef) {
    def respondsTo(address: Address) = primaryAddress == address || otherAddresses(address)
  }
  // can also send fs20 Packet to this actor, indicating that packet was received. Reply will be given.
  /** Request to set the given primaryAddress to a value. Reply will be given. */
  case class Want(primaryAddress: Address, brightness: Command)
  /** Request to send out the next radio packet, if needed. Reply will be given. */
  case object Proceed  // go ahead and send stuff out
  
  // responses:
  /** No more radio packets need to be sent */
  case object Idle     // don't need to send stuff out
  /** More radio packets need to be sent, and will be done upon receiving the next Proceed */
  case object WantToProceed // need to send stuff out
}
