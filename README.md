URI Template
=============
Complete implementation of [RFC-6570 (uri-templates)](http://tools.ietf.org/html/rfc6570) in Scala

[Apache 2 Licenced](http://www.apache.org/licenses/LICENSE-2.0)

Example of usage (see the tests for more)
--------------------------------------------------
```scala
import uritemplate._
import Syntax._

val template = URITemplate("http://example.com/hello/{variable}")
val expanded = template expand ("variable" := "world")

expanded == "http://example.com/hello/world"
```

Named variables (using macros)
---------------------------------
```scala
import uritemplate._
import Named._

val template = URITemplate("http://example.com/hello/{variable}")

//single
val variable = "world"
val expanded = template.named(variable)

//nested
object nested {
  val variable = "world"
}

val nestedTemplate = URITemplate("http://example.com/hello/{nested.variable}")
val nestedExpanded = nestedTemplate.named(nested.variable)
```

Interpolated (using macros)
---------------------------
```scala
import uritemplate._
import Magic._ // working title

val variable = "world"
val template = magic("http://example.com/hello/{variable}")

template == "http://example.com/hello/world"
```

Dependencies
------------

SBT:
	
	libraryDependencies += "no.arktekk" %% "uri-template" % "1.1"

Maven:

	<dependency>
	  <groupId>no.arktekk</groupId>
	  <artifactId>uri-template_${scalaVersion}</artifactId>
	  <version>1.1</version>
	</dependency>
