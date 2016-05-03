package nl.ypmania.spark.json

/**
 * Buffers an incoming stream of JSON tokens when its selector matches. 
 * 
 * For any condition that yields a match on the child tokens, the child parsers are invoked
 * with all of the buffered tokens. Otherwise, the child parsers are skipped.
 */
class Buffer[T](selector: Selector, child:(BufferedSelector, Parser[T])) {
  var selected = false
  var buffer = Seq.empty[JSONToken] // TODO max size 
  
  def handle(t: JSONToken) {
    if (selector.select(t)) {
      if (!selected) {
        // new selection block
        selected = true
      } 
      buffer :+= t
      child._1.accept(t)
    } else {
      if (selected) {
        if (child._1.select()) {
          // child matched, empty buffer into child parser, and also this one.
          buffer.foreach(child._2.accept)
          child._2.accept(t)
        }
    	  selected = false
      } 
    }
  }
}