package uritemplate

import Syntax._

import org.json4s._, native._
import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers._
import java.io.{InputStreamReader, FileInputStream}

abstract class JsonSpec(file:String) extends FunSpec {
  override def suiteName = file

  val JObject(examples) = parseJson(new InputStreamReader(new FileInputStream("uritemplate-test/"+file)))
  for((name, example) <- examples){
    describe(name){
      val JObject(variables) = example \ "variables"
      val args = variables.map{
        case (field, JString(variable)) =>
          field := variable
        case (field, JInt(variable))    =>
          field := variable.intValue()
        case (field, JDouble(variable)) =>
          field := variable
        case (field, JArray(seq)) =>
          field := seq.map{ case JString(str) => str }
        case (field, JObject(fields)) =>
          field := fields.map{ case (k, JString(v)) => (k, v) }
        case (field, JNull) =>
          field := None
      }

      val JArray(testcases) = example \ "testcases"

      testcases.foreach{
        case JArray(List(JString(template), JString(exact))) => it(template){
          URITemplate(template).expand(args :_*) should be (exact)
        }
        case JArray(List(JString(template), JArray(oneOf)))  => it(template){
          oneOf.map{ case JString(s) => s } should contain(URITemplate(template).expand(args :_*))
        }
        case JArray(List(JString(template), JBool(false))) => it(template){
          URITemplate.parse(template) match {
            case Left(msg) =>
              info(msg)
            case Right(t) =>
              val caught = evaluating(t.expand(args :_*)) should produce[RuntimeException]
              info(caught.getMessage)
          }
        }
      }
    }
  }
}

class ExtendedTests extends JsonSpec("extended-tests.json")
class NegativeTests extends JsonSpec("negative-tests.json")
class SpecExamplesBySection extends JsonSpec("spec-examples-by-section.json")
class SpecExamples extends JsonSpec("spec-examples.json")
