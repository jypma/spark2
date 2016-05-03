package nl.ypmania.spark.json

/**
 * Matches a particular JSON node by observing only tokens, and matches immediately once it sees a
 * node of interest. It must continue returning true while inside the selected series of tokens.
 * 
 * For example, a selector matching a sub-object, should return true from StartObject up until but 
 * not including EndObject.
 */
trait Selector {
  def select(token: JSONToken): Boolean
}