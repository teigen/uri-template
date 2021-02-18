import sbt._
import sbt.Keys._
import xml.Group

object Build extends sbt.Build {

  lazy val buildSettings = Defaults.defaultSettings ++ Seq(
    organization := "no.arktekk",
    scalaVersion := "2.13.4",
    crossScalaVersions := Seq("2.11.2","2.10.1", "2.12.13", "2.13.4"),
    publishTo <<= (version) apply {
      (v: String) => if (v.trim().endsWith("SNAPSHOT")) Some(Resolvers.sonatypeNexusSnapshots) else Some(Resolvers.sonatypeNexusStaging)
    },
    pomIncludeRepository := { x => false },
    credentials += Credentials(Path.userHome / ".sbt" / "arktekk-credentials")
  )

  lazy val root = Project(
    id = "root",
    base = file("."),
    settings = buildSettings ++ Seq(
      description := "URI Template",
      name := "uri-template",
      libraryDependencies ++= Seq(
        "org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.2",
        "org.scalatest" %% "scalatest" % "3.2.3" % "test"
      ),
      libraryDependencies <++= (scalaBinaryVersion) apply {(sv: String) =>
        if(sv == "2.11") Seq("org.scala-lang.modules" % "scala-parser-combinators_2.11" % "1.0.2") else Nil
      },
    manifestSetting
    ) ++ mavenCentralFrouFrou
  )

  object Resolvers {
    val sonatypeNexusSnapshots = "Sonatype Nexus Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
    val sonatypeNexusStaging = "Sonatype Nexus Staging" at "https://oss.sonatype.org/service/local/staging/deploy/maven2"
  }

  lazy val manifestSetting = packageOptions <+= (name, version, organization) map {
    (title, version, vendor) =>
      Package.ManifestAttributes(
        "Created-By" -> "Simple Build Tool",
        "Built-By" -> System.getProperty("user.name"),
        "Build-Jdk" -> System.getProperty("java.version"),
        "Specification-Title" -> title,
        "Specification-Version" -> version,
        "Specification-Vendor" -> vendor,
        "Implementation-Title" -> title,
        "Implementation-Version" -> version,
        "Implementation-Vendor-Id" -> vendor,
        "Implementation-Vendor" -> vendor
      )
  }

  // Things we care about primarily because Maven Central demands them
  lazy val mavenCentralFrouFrou = Seq(
    homepage := Some(new URL("http://github.com/arktekk/uri-template")),
    startYear := Some(2011),
    licenses := Seq(("Apache 2", new URL("http://www.apache.org/licenses/LICENSE-2.0.txt"))),
    pomExtra <<= (pomExtra, name, description) {(pom, name, desc) => pom ++ Group(
      <scm>
        <url>https://github.com/arktekk/uri-template</url>
        <connection>scm:git:git://github.com/arktekk/uri-template.git</connection>
        <developerConnection>scm:git:git@github.com:arktekk/uri-template.git</developerConnection>
      </scm>
      <developers>
        <developer>
          <id>teigen</id>
          <name>Jon-Anders Teigen</name>
          <url>http://twitter.com/jteigen</url>
        </developer>
      </developers>
    )}
  )
}
