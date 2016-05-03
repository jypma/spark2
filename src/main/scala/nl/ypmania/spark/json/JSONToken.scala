package nl.ypmania.spark.json

sealed trait JSONToken

case object StartObject extends JSONToken
case object EndObject extends JSONToken
case object StartArray extends JSONToken
case object EndArray extends JSONToken
case class FieldName(name: String) extends JSONToken
case class StringValue(value: String) extends JSONToken
case class NumberValue(value: String) extends JSONToken
case object FalseValue extends JSONToken
case object TrueValue extends JSONToken
case object NullValue extends JSONToken

