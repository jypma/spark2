enablePlugins(JavaAppPackaging)

scalaVersion := "2.12.4"
 
libraryDependencies ++= Seq(
  "akka-actor"
).map(a => "com.typesafe.akka" %% a % "2.5.12") 

libraryDependencies ++= Seq( 
  "akka-http"
).map(a => "com.typesafe.akka" %% a % "10.1.1") 

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % "test"
