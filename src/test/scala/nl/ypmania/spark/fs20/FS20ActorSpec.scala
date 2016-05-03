package nl.ypmania.spark.fs20

class FS20ActorSpec {
  "receiving Have for an actuator that not wanted yet does not cause a replan"
  
  "receiving Want for an actuator that is not had (registered) yet does not cause a replan"

  "receving Have for an actuator that is wanted with a different brightness, cause a replan"
  
  "receiving Want for an actuator that is had with a different brightess, cause a replan"
}