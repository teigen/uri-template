package uritemplate

import language.experimental.macros
import reflect.macros.Context
import uritemplate.Syntax.CanBeVar

object Named {
  implicit class NamedExpand(val template:URITemplate) extends AnyVal {
    def named(values:Any*):String = macro Named.impl
  }

  private object Named {
    def impl(c:Context)(values:c.Expr[Any]*):c.Expr[String] = {
      import c.universe._

      val Apply(_, List(template)) = c.prefix.tree

      def name(tree:Tree):String = tree match {
        case Ident(n)      => n.toString
        case Select(q, n)  => s"${name(q)}.$n"
        case Apply(fun, _) => name(fun)
        case what          => c.abort(c.enclosingPosition, showRaw(what))
      }

      val vars = values.toList.map{ expr =>
        val tree     = expr.tree
        val tpe      = appliedType(c.typeOf[CanBeVar[_]], List(tree.tpe))
        val instance = c.inferImplicitValue(tpe)
        val nme      = c.Expr[String](Literal(Constant(name(tree))))
        val variable = c.Expr[Option[Variable]](Apply(Select(instance, newTermName("canBe")), List(tree)))
        reify((nme.splice, variable.splice)).tree
      }

      c.Expr[String](Apply(Select(template, newTermName("expand")), vars))
    }
  }
}
