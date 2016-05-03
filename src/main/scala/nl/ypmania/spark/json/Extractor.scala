package nl.ypmania.spark.json

trait Extractor[T] {
  parent =>
  
  def accept(t: JSONToken)
  def extract(): Option[T]
  
  def map[U](f: T => U) = new Extractor[U] {
    override def accept(t: JSONToken) = parent.accept(t)
    override def extract() = parent.extract().map(f)
  }
}

object Extractor {
  def stringField(Name: String) = new Extractor[String] {
    var matched = false
    var nestedObjects = 0
    var saved:Option[String] = None
    
    override def accept(t: JSONToken) = t match {
      case FieldName(Name) if nestedObjects == 0 =>
        matched = true
      case StringValue(value) if matched =>
        saved = Some(value)
        matched = false
      case NumberValue(value) if matched =>
        saved = Some(value)
        matched = false
      case StartObject =>
        nestedObjects += 1
      case EndObject =>
        nestedObjects -= 1
      case _ =>
        matched = false
    }
    
    override def extract() = {
      val x = saved
      saved = None
      x
    }
  }
  
  def obj[T](inner: Extractor[T]) = new Extractor[T] {
    var matched = false
    var nestedObjects = 0
    
    override def accept(t: JSONToken) = {
      t match {
        case StartObject if nestedObjects == 0 =>
          matched = true
          nestedObjects += 1
        case StartObject if nestedObjects > 0 =>
          nestedObjects += 1
          inner.accept(t)
        case EndObject if nestedObjects > 1 =>
          nestedObjects -= 1
          inner.accept(t)
        case EndObject if nestedObjects == 1 =>
          nestedObjects -= 1
          matched = false
        case _ if matched =>
          inner.accept(t)
        case _ if !matched =>
      }
    }
    
    override def extract() = inner.extract()
  }
  
  def objField[T](Name: String, inner: Extractor[T]) = new Extractor[T] {
    var matched = false
    var nestedObjects = 0
    
    override def accept(t: JSONToken) = t match {
      case FieldName(Name) if nestedObjects == 0 =>
        matched = true
      case StartObject if matched && nestedObjects == 0 =>
        nestedObjects += 1
      case StartObject if matched && nestedObjects > 0 =>
        nestedObjects += 1
        inner.accept(t)
      case EndObject if matched && nestedObjects > 1 =>
        nestedObjects -= 1
        inner.accept(t)
      case EndObject if matched && nestedObjects == 1 =>
        nestedObjects -= 1
        matched = false
      case EndObject if matched && nestedObjects == 0 =>    // wasn't an object, outer object ended
        matched = false
      case FieldName(_) if matched && nestedObjects == 0 => // wasn't an object
        matched = false
      case _ if matched =>
        inner.accept(t)
      case _ if !matched =>
        
    }
    
    override def extract() = inner.extract()
  }
  
  def option[T](inner: Extractor[T]) = new Extractor[Option[T]] {
    override def accept(t: JSONToken) = inner.accept(t)
    override def extract() = Some(inner.extract())
  }
  
  def apply[T1,T2](e1: Extractor[T1], e2: Extractor[T2]) = new Extractor[(T1, T2)] {
    override def accept(t: JSONToken) = {
      e1.accept(t)
      e2.accept(t)
    }
    
    override def extract() = for (v1 <- e1.extract(); v2 <- e2.extract()) yield (v1, v2)
  }
  
  def apply[T1,T2,T3](e1: Extractor[T1], e2: Extractor[T2], e3: Extractor[T3]) = new Extractor[(T1, T2, T3)] {
    override def accept(t: JSONToken) = {
      e1.accept(t)
      e2.accept(t)
      e3.accept(t)
    }
    
    override def extract() = for (v1 <- e1.extract(); v2 <- e2.extract(); v3 <- e3.extract()) yield (v1, v2, v3)
  }
  
}