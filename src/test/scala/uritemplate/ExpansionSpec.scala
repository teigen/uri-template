package uritemplate

import org.scalatest.prop.TableDrivenPropertyChecks.forAll
import org.scalatest.prop.Tables.Table
import org.scalatest.propspec.AnyPropSpec

trait ExpansionSpec extends AnyPropSpec {

  val variables: Map[String, Option[Variable]]

  override def suiteName = name

  def renderVariables = {
    val keyLength = variables.keySet.map(_.length()).max
    val strings   = variables.map { case (key, value) =>
      val padded: String = key + " " * (keyLength - key.length())
      padded + " := " + value
    }
    strings.mkString("\t", "\n\t", "")
  }

  def name: String

  def example(name: String)(examples: (String, String)*): Unit = {
    property(name) {
      val table = Table(("Expression", "Expansion"), examples: _*)
      forAll(table) { (exp, ex) =>
        assert((URITemplate(exp) expand variables) === ex)
      }
    }
  }

}
