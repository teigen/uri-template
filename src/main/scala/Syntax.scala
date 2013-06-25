package uritemplate

object Syntax extends Syntax

trait Syntax {
  implicit def varString(s:String) = Var(s)
}

case class Var(s:String){
  def :=[V : CanBeVar](v:V):(String, Option[Variable]) = (s, CanBeVar[V].canBe(v))
}

object CanBeVar extends CanBeVars {
  def apply[V](implicit canBe:CanBeVar[V]) = canBe
}

trait CanBeVar[-V] {
  def canBe(v:V):Option[Variable]
}

trait CanBeVars extends LowerPriorityCanBeVars {
  implicit val stringCanBe = new CanBeVar[String]{
    def canBe(v: String) = Option(v).map(vv => SequentialVar(Seq(vv)))
  }

  implicit val int = new CanBeVar[Int]{
    def canBe(v: Int) = Option(v).map(vv => SequentialVar(Seq(vv.toString)))
  }

  implicit val double = new CanBeVar[Double]{
    def canBe(v: Double) = Option(v).map(vv => SequentialVar(Seq(vv.toString)))
  }

  implicit val tuple2CanBe = new CanBeVar[(String, String)]{
    def canBe(v: (String, String)) = Option(v).map(vv => AssociativeVar(Seq(vv)))
  }

  implicit val seqStringCanBe = new CanBeVar[Seq[String]]{
    def canBe(v: Seq[String]) = Option(v).map(SequentialVar)
  }

  implicit def optionCanBe[C : CanBeVar] = new CanBeVar[Option[C]]{
    def canBe(v: Option[C]) = v.flatMap(CanBeVar[C].canBe)
  }

  implicit val optionNothingCanBe = new CanBeVar[Option[Nothing]]{
    def canBe(v: Option[Nothing]) = v
  }
}

trait LowerPriorityCanBeVars {
  implicit val seqTupleCanBe = new CanBeVar[Seq[(String, String)]]{
    def canBe(v: Seq[(String, String)]) = Option(v).map(AssociativeVar)
  }
}
