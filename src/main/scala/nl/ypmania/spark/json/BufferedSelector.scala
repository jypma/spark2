package nl.ypmania.spark.json

trait BufferedSelector {
  def accept(t: JSONToken): Unit
  def select(): Boolean
}