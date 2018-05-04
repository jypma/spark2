enablePlugins(JavaAppPackaging)

scalaVersion := "2.12.5"
 
libraryDependencies ++= Seq(
  "akka-actor", 
  "akka-http-experimental", 
  "akka-http-spray-json-experimental"
).map(a => "com.typesafe.akka" %% a % "2.5.12") 

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % "test"
