package uritemplate

import language.experimental.macros
import reflect.macros.Context

object Magic {

  def magic(s:String) = macro magic_impl

  def magic_impl(c:Context)(s:c.Expr[String]) = {
    import c.universe._

    def generate(template:URITemplate) = {
      val vars = template.variables.map{ name =>
        val ref      = c.typeCheck(Ident(newTermName(name)))
        val tpe      = appliedType(typeOf[Syntax.CanBeVar[_]], List(ref.tpe))
        val instance = c.inferImplicitValue(tpe)

        val nme      = c.Expr[String](Literal(Constant(name)))
        val variable = c.Expr[Option[Variable]](Apply(Select(instance, newTermName("canBe")), List(ref)))

        reify{ (nme.splice, variable.splice) }.tree
      }.toList

      val expand = Apply(Select(reify{ URITemplate(s.splice) }.tree, newTermName("expand")), vars)

      c.Expr[String](expand)
    }

    s.tree match {
      case Literal(Constant(string:String)) =>
        URITemplate.parse(string).fold(x => c.abort(c.enclosingPosition, x), x => generate(x))
      case _ =>
        c.abort(c.enclosingPosition, "unknown tree " + show(s))
    }
  }
}
