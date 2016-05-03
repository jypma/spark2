package nl.ypmania.spark.main

import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.actor.ActorSystem
import akka.actor.Props
import nl.ypmania.spark.zone.Zone

object Main extends App {
  implicit val system = ActorSystem("my-system")
  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher
 
  val route =
    path("hello") {
      get {
        complete("hello world")
      }
    }
 
  val mainZone = system.actorOf(Props(new Zone(system.settings.config.getConfig("spark.zones"))), "zones")
  val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)
 
  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
  Console.readLine() // for the future transformations
  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ ⇒ system.terminate()) // and shutdown when done
}