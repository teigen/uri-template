package uritemplate

import util.parsing.combinator.ImplicitConversions

object Variable {
  case class Single(variable:String) extends Variable
  case class Sequential(variable:Seq[String]) extends Variable
  case class Associative(variable:Seq[(String, String)]) extends Variable
}

sealed trait Variable

object URITemplate {
  def apply(s:String) = parse(s).fold(sys.error, identity)
  def parse(s:String) = URITemplateParser.parse(s)

  case class VarSpec(name: String, modifier: Option[Modifier])

  sealed trait Expansion {
    def expand(variables:Map[String, Option[Variable]]):String
  }

  case class Literal(value:String) extends Expansion {
    def expand(variables: Map[String, Option[Variable]]) = value
  }

  object Expression {
    import Variable._

    def expand(variables:Map[String, Option[Variable]], variableList:List[VarSpec], first:String, sep:String, named:Boolean, ifemp:String, allow:String => String) = {
      val expanded = variableList.flatMap{ case VarSpec(n, modifier) =>

        def name(s:String) = if(named) n+"="+s else s

        variables.getOrElse(n, None).flatMap{
          case Sequential(Seq()) | Associative(Seq()) =>
            if(named && modifier == None) Some(n+ifemp) else None

          case Single("") =>
            Some(if(named)n+ifemp else ifemp)

          case Single(variable) => Some(modifier match {
            case None                 => name(allow(variable))
            case Some(Prefix(length)) => name(allow(variable.take(length)))
            case Some(Explode)        => name(allow(variable))
          })

          case Sequential(variable) => Some(modifier match {
            case None => name(variable.map(allow).mkString(","))
            case Some(Prefix(length)) => name(variable.map(v => allow(v.take(length))).mkString(","))
            case Some(Explode) => variable.map(n => name(allow(n))).mkString(sep)
          })

          case Associative(variable) => Some(modifier match {
            case None => name(variable.flatMap{ case (k,v) => Seq(allow(k), allow(v)) }.mkString(","))
            case Some(Explode) => variable.map{ case (k, v) => allow(k)+"="+allow(v)}.mkString(sep)
            case Some(Prefix(_)) => throw new IllegalArgumentException("Not allowed by RFC-6570")
          })
        }
      }
      if(expanded.isEmpty) "" else expanded.mkString(first, sep, "")
    }
  }

  sealed abstract class Expression(first:String, sep:String, named:Boolean, ifemp:String, allow:String => String) extends Expansion {
    def variableList:List[VarSpec]
    def expand(variables: Map[String, Option[Variable]]) = Expression.expand(variables, variableList, first, sep, named, ifemp, allow)
  }
  case class Simple(variableList:List[VarSpec])            extends Expression("",  ",", false, "",  Allow.U)
  case class Reserved(variableList:List[VarSpec])          extends Expression("",  ",", false, "",  Allow.UR)
  case class Fragment(variableList:List[VarSpec])          extends Expression("#", ",", false, "",  Allow.UR)
  case class Label(variableList:List[VarSpec])             extends Expression(".", ".", false, "",  Allow.U)
  case class PathSegment(variableList:List[VarSpec])       extends Expression("/", "/", false, "",  Allow.U)
  case class PathParameter(variableList:List[VarSpec])     extends Expression(";", ";", true,  "",  Allow.U)
  case class Query(variableList:List[VarSpec])             extends Expression("?", "&", true,  "=", Allow.U)
  case class QueryContinuation(variableList:List[VarSpec]) extends Expression("&", "&", true,  "=", Allow.U)

  sealed trait Modifier
  case class Prefix(maxLength: Int) extends Modifier
  case object Explode extends Modifier

  object Allow {
    def U(s:String) = URITemplateParser.u(s)
    def UR(s:String)= URITemplateParser.ur(s)
  }

  object URITemplateParser {
    import util.parsing.combinator.RegexParsers
    import util.parsing.input.CharSequenceReader

    def parse(s:String):Either[String, URITemplate] = {
      val syntax = new URITemplateParsers
      import syntax._
      phrase(template)(new CharSequenceReader(s)) match {
        case Success(result, _) => Right(result)
        case n                  => Left(n.toString)
      }
    }

    def u(s:String):String = {
      val syntax = new URITemplateParsers
      import syntax._
      phrase(U.*)(new CharSequenceReader(s)) match {
        case Success(result, _) => result.mkString
        case n                  => sys.error(n.toString)
      }
    }

    def ur(s:String):String = {
      val syntax = new URITemplateParsers
      import syntax._
      phrase(`U+R`.*)(new CharSequenceReader(s)) match {
        case Success(result, _) => result.mkString
        case n                  => sys.error(n.toString)
      }
    }

    class URITemplateParsers extends RegexParsers with ImplicitConversions {

      def encode(char:Char) = char.toString.getBytes("UTF-8").map(b => "%02X".format(b)).mkString("%", "%", "")

      override def skipWhitespace = false

      lazy val template = (expression | lit).* ^^ { v => new URITemplate(v) }

      lazy val `U+R` = reserved | U
      lazy val U     = unreserved | anyChar ^^ encode

      lazy val lit = rep1(not("{" | "}") ~> anyChar) ^^ { cs => Literal(cs.mkString) }

      lazy val expression = "{" ~! operator.? ~ variableList <~ "}" ^^ {
        case _ ~ Some(op) ~ vars => op(vars)
        case _ ~ _        ~ vars => Simple(vars)
      }

      lazy val operator =
        ( "+" ^^^ Reserved
        | "#" ^^^ Fragment
        | "." ^^^ Label
        | "/" ^^^ PathSegment
        | ";" ^^^ PathParameter
        | "?" ^^^ Query
        | "&" ^^^ QueryContinuation )

      lazy val variableList = repsep(varspec, ",")
      lazy val varspec = varname ~ modifierLevel4.? ^^ VarSpec
      lazy val varname = varchar ~ rep(".".? ~ varchar ^^ { case opt ~ v => opt.getOrElse("") + v }) ^^ { case head ~ tail => head + tail.mkString }
      lazy val varchar = ALPHA | DIGIT | "_" | pctEncoded

      lazy val modifierLevel4 = prefix | explode
      lazy val prefix = ":" ~> "[1-9]\\d{0,3}".r ^^ { case p => Prefix(p.toInt) }
      lazy val explode = "*" ^^^ Explode

      lazy val ALPHA = "[A-Za-z]".r
      lazy val DIGIT = "[0-9]".r
      lazy val HEXDIG = "[0-9A-Fa-f]".r

      lazy val pctEncoded = "%" ~ HEXDIG ~ HEXDIG ^^ { case pct ~ h1 ~ h2 => pct + h1 + h2 }
      lazy val unreserved = (ALPHA | DIGIT | "-" | "." | "_" | "~")
      lazy val reserved = genDelims | subDelims
      lazy val genDelims = ":" | "/" | "?" | "#" | "[" | "]" | "@"
      lazy val subDelims = "!" | "$" | "&" | "'" | "(" | ")" | "*" | "+" | "," | ";" | "="

      lazy val anyChar = elem("anyChar", _ != 26.toChar)
    }
  }
}

class URITemplate(expansions: List[URITemplate.Expansion]){
  def expand(variables:Map[String, Option[Variable]]) = expansions.map(_.expand(variables)).mkString  
  def expand(variables:(String, Option[Variable])*):String = expand(variables.toMap)

  def variables = expansions.flatMap{
    case e:URITemplate.Expression => e.variableList.map(_.name)
    case _ => Nil
  }.toSet
}


