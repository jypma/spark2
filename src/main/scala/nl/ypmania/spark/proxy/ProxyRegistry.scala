package nl.ypmania.spark.proxy

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.ExtensionId
import akka.actor.ExtensionIdProvider
import akka.actor.ExtendedActorSystem
import akka.actor.Props
import akka.actor.Extension
import akka.io.IO
import akka.io.Udp
import java.net.InetSocketAddress
import akka.actor.Stash
import akka.io.Udp.CommandFailed
import akka.actor.ActorLogging
import nl.ypmania.spark.fs20
import nl.ypmania.spark.rf12
import nl.ypmania.spark.visonic

object ProxyRegistry {
  case class Transmit(preferredProxies: Seq[String], msg: RadioMessage)
  
  def apply(system: ActorSystem): ActorRef = ExtId(system).actor

  private class Ext(system: ActorSystem) extends Extension {
    val actor = system.actorOf(Props[ProxyRegistry], "proxyRegistry")
  }
  
  private object ExtId extends ExtensionId[Ext] with ExtensionIdProvider {
    def createExtension(system: ExtendedActorSystem) = new Ext(system)
    def lookup() = ExtId
  }
}

class ProxyRegistry extends Actor with ActorLogging with Stash {
  import ProxyRegistry._
  import context.system
  
  val port = context.system.settings.config.getInt("spark.proxies.port")
  IO(Udp) ! Udp.Bind(self, new InetSocketAddress("localhost", port))
  
  def receive = {
    case Udp.Bound(local) =>
      unstashAll()
      context.become(ready(sender()))
    case CommandFailed(_) =>
      log.error("Cannot bind to port {}. Stopping actor.", port)
      context.stop(self)
    case _ =>
      stash()
  }
  
  def ready(socket: ActorRef): Receive =  {
    val knownProxies = collection.mutable.Map.empty[String, InetSocketAddress]
    
    {
      case Udp.Received(data, remote) =>
        // reset timeout on proxy for that sender
        RadioMessage(data) match {
          case Some(Ping(id)) =>
            log.debug("Received ping from {} which has ID {}", remote, id);
            if (knownProxies.put(id, remote).isEmpty) {
              log.info("Joined {} at {}", id, remote)                      
            }
            log.debug("Known proxies: {}", knownProxies)
          
          case Some(msg: fs20.Packet) =>
            
          case Some(msg: rf12.Packet) =>
            
          case Some(msg: visonic.Packet) =>
            
          case _ =>
            log.warning("Couldn't parse packet: {}", data)
        }
        
      case Transmit(preferredProxies, msg) =>
        
        
      case Udp.Unbind  => socket ! Udp.Unbind
      case Udp.Unbound => context.stop(self)
    }
  }
}