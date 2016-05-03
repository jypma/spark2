package nl.ypmania.spark.json

import org.scalatest.Matchers
import org.scalatest.WordSpec

class JSONSpec extends WordSpec with Matchers {
  
  import Extractor._
  case class DTO(s: String, i: Integer, s2: String)
  
  "An extractor for a simple DTO with optional sub-object" should {
    val extractor = Extractor.obj(
      Extractor(
        stringField("s"),
        stringField("i").map(Integer.parseInt),
        option(objField("o",
          Extractor(
            stringField("s1"),
            stringField("s2")
          )
        ))
      ).map { 
        case (s,i, None) => DTO(s,i,"")
        case (s,i, Some((s1, s2))) => DTO(s,i,s1)
      } 
    )
    
    "extract an instance from JSON with both fields present, and be reusable" in {
      Seq(StartObject, FieldName("s"), StringValue("s_value"),
          FieldName("i"), NumberValue("123"), EndObject).foreach(extractor.accept)
          
      extractor.extract() should be (Some(DTO("s_value", 123, "")))
      extractor.extract() should be (None)
      
      Seq(StartObject, FieldName("s"), StringValue("s_value2"),
          FieldName("i"), NumberValue("1233"), EndObject).foreach(extractor.accept)

      extractor.extract() should be (Some(DTO("s_value2", 1233, "")))          
    }
    
    "extract the sub-object when it is present, and ignore ununknown fields" in {
      Seq(StartObject, 
          FieldName("s"), StringValue("s_value"),
          FieldName("i"), NumberValue("123"),
          FieldName("o"), StartObject,
              FieldName("s1"), StringValue("hello"),
              FieldName("s2"), StringValue("world"),
              FieldName("foo"), StringValue("unused"),
          EndObject,
          EndObject).foreach(extractor.accept)
      
      extractor.extract() should be (Some(DTO("s_value", 123, "hello")))
    }
    
  }
  
  "An extractor with further conditions on some fields" should {
    val extractor = obj(
      Extractor(
        stringField("s"),
        stringField("i").map(Integer.parseInt)
      ).map { case (s,i) => DTO(s,i, "") } 
    )
    
  }
}