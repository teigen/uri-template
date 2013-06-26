package uritemplate

object Syntax extends Syntax

trait Syntax {
  implicit def varString(s:String) = Var(s)
}

case class Var(s:String){
  def :=[V : CanBeVar](v:V):(String, Option[Variable]) = (s, implicitly[CanBeVar[V]].canBe(v))
}

object CanBeVar extends CanBeVars {
  def canBe[V](f:V => Variable):CanBeVar[V] = new CanBeVar[V]{
    def canBe(v: V) = Option(v).map(f)
  }
}

trait CanBeVar[-V] {
  def canBe(v:V):Option[Variable]
}

trait CanBeVars extends LowerPriorityCanBeVars {
  implicit val string    = CanBeVar.canBe[String](s => SequentialVar(Seq(s)))
  implicit val int       = CanBeVar.canBe[Int](s => SequentialVar(Seq(s.toString)))
  implicit val double    = CanBeVar.canBe[Double](s => SequentialVar(Seq(s.toString)))
  implicit val tuple2    = CanBeVar.canBe[(String, String)](s => AssociativeVar(Seq(s)))
  implicit val seqString = CanBeVar.canBe[Seq[String]](SequentialVar)
  implicit def option[C : CanBeVar] = new CanBeVar[Option[C]]{
    def canBe(v: Option[C]) = v.flatMap(implicitly[CanBeVar[C]].canBe)
  }
  implicit val optionNothing = new CanBeVar[Option[Nothing]]{
    def canBe(v: Option[Nothing]) = v
  }
}

trait LowerPriorityCanBeVars {
  implicit val traversableTuple = CanBeVar.canBe[Traversable[(String, String)]](vv => AssociativeVar(vv.toSeq))
}
