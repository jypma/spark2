package nl.ypmania.spark.fs20

import akka.actor.Actor
import scala.concurrent.duration._
import akka.actor.ActorRef
import nl.ypmania.spark.zone.Zone

class FS20Actor extends Actor {
  import FS20Actor._
  
  private var have = State(Map.empty)
  private var want = State(Map.empty)
  private var plan = Seq.empty[Command]
  private var zoneForAddress = Map.empty[Address,ActorRef]
  
  override def receive = {
    case Have(actuator, brightness) =>
      have += actuator -> brightness
      zoneForAddress = have.actuators.keySet.map(a => a.otherAddresses.map(_ -> a.zone) + (a.primaryAddress -> a.zone)).flatten.toMap
      for (b <- want.get(actuator) if b != brightness) {
        replan()
      }
      
    case Want(actuator, brightness) =>
      want += actuator -> brightness
      for (b <- have.get(actuator) if b !=  brightness) {
        replan()
      }
      
    case Proceed if !plan.isEmpty =>
      val command = plan.head
      zoneForAddress(command.address) ! Zone.SendToProxy(command)
      plan = plan.tail
      //sender ! Send(proxies, cmd, 75.milliseconds)
    
    case Proceed if plan.isEmpty =>
      sender ! Idle
  }
  
  def replan() {
	  plan = want.planFrom(have)
	  if (plan.isEmpty) {
	    context.parent ! Idle
	  } else {
	    context.parent ! Awaiting
	  }
  }
}

object FS20Actor {
  private[fs20] case class State(actuators: Map[Actuator, Brightness]) {
    def + (t: (Actuator, Brightness)) = State(actuators + t)
    
    def changesFrom (have: State): Map[Brightness, Set[Actuator]] = {
      val diff = for (
        (a, b) <- actuators;
        haveIt = have.get(a)
        if haveIt.isDefined && haveIt.get != b
      ) yield (a, b)
      diff.groupBy(_._2).mapValues(_.keys.toSet)
    }
    
    def get(a: Actuator) = actuators.get(a)
    
    def getBrightnesses(address: Address): Set[Brightness] = 
      actuators.filterKeys(_.respondsTo(address)).values.toSet
      
    def planFrom (have: State): Seq[Command] = {
      val plan = Seq.newBuilder[Command]
      
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
          plan += Command(nibble, brightness)
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
          plan += Command(a.primaryAddress, brightness)
        }
      }
      
      plan.result()
    }
  }
  
  case class Have(actuator: Actuator, brightness: Brightness)
  case class Want(actuator: Actuator, brightness: Brightness)
  
  // the following should be pushed down into a generic radio package
  case object Proceed  // go ahead and send stuff out. reply:
  case class Send(proxy: String, cmd: Any, duration: FiniteDuration)  // radio is in use this long
  
  case object Idle     // don't need to send stuff out
  
  case object Awaiting // need to send stuff out
}