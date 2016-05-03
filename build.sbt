enablePlugins(JavaAppPackaging)

scalaVersion := "2.11.7"
 
libraryDependencies ++= Seq(
  "akka-actor", 
  "akka-http-experimental", 
  "akka-http-spray-json-experimental"
).map(a => "com.typesafe.akka" %% a % "2.4.2") 

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.6" % "test"
