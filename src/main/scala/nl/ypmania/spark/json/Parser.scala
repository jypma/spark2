package nl.ypmania.spark.json

trait Parser[T] {
  def accept(t: JSONToken)
}