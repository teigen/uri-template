URI Template
=============
Complete implementation of draft-8 of [uri-templates](https://datatracker.ietf.org/doc/draft-gregorio-uritemplate/) in Scala

[Apache 2 Licenced](http://www.apache.org/licenses/LICENSE-2.0)

Example of usage (see the tests for more)
--------------------------------------------------

	import uritemplate._
	import Syntax._
	
	val template = URITemplate("http://example.com/hello/{variable}")
	val expanded = template expand ("variable" := "world")
	
	expanded == "http://example.com/hello/world"