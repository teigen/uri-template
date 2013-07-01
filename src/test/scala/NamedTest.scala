package uritemplate

import org.scalatest.FunSuite

class NamedTest extends FunSuite {
  import Named._

  test("literal"){
    val a = "hello"
    val b = Seq("aa", "bb", "cc")

    assert("http://hello/?b=a,b,c" === URITemplate("http://{a}/{?b:1}").named(a, b))
  }

  test("variable"){
    val a = "hello"
    val b = Seq("aa", "bb", "cc")

    val template = URITemplate("http://{a}{/b*}")

    assert("http://hello/aa/bb/cc" === template.named(a, b))
  }

  test("object dot"){
    object a {
      object b {
        val c = "x"
      }
    }

    val y = "yy"

    assert(URITemplate("{y}-{a.b.c}").named(a.b.c, y) === "yy-x")
  }

  test("method dot"){
    class B {
      val a = "x"
    }
    class C {
      def b() = new B
    }
    val c = new C

    assert(URITemplate("{c.b.a}").named(c.b().a) === "x")
  }
}
