import uritemplate.Magic._

object MagicTest extends App {

  val a = "hello"
  val b = Seq("aa", "bb", "cc")

  println(magic("http://{a}/{?b:1}"))

}
