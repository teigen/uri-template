package uritemplate

object Syntax {
  implicit class Var(s:String){
    def :=[V : CanBeVar](v:V):(String, Option[Variable]) = (s, implicitly[CanBeVar[V]].canBe(v))
  }

  object CanBeVar extends CanBeVars {
    def apply[V](f:V => Variable):CanBeVar[V] = new CanBeVar[V]{
      def canBe(v: V) = Option(v).map(f)
    }
  }

  trait CanBeVar[-V] {
    def canBe(v:V):Option[Variable]
  }

  trait CanBeVars extends LowerPriorityCanBeVars {
    import Variable._

    implicit val string    = CanBeVar[String](s => Sequential(Seq(s)))
    implicit val int       = CanBeVar[Int](s => Sequential(Seq(s.toString)))
    implicit val double    = CanBeVar[Double](s => Sequential(Seq(s.toString)))
    implicit val tuple2    = CanBeVar[(String, String)](s => Associative(Seq(s)))
    implicit val seqString = CanBeVar[Seq[String]](Sequential)
    implicit def option[C : CanBeVar] = new CanBeVar[Option[C]]{
      def canBe(v: Option[C]) = v.flatMap(implicitly[CanBeVar[C]].canBe)
    }
    implicit val optionNothing = new CanBeVar[Option[Nothing]]{
      def canBe(v: Option[Nothing]) = v
    }
  }

  trait LowerPriorityCanBeVars {
    import Variable._
    implicit val traversableTuple = CanBeVar[Traversable[(String, String)]](vv => Associative(vv.toSeq))
  }
}
