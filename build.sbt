ThisBuild / description := "URI Template"

ThisBuild / scalacOptions := Seq("-deprecation", "-unchecked")

lazy val root = (project in file("."))
  .settings(
    Seq(
      organization := "no.arktekk",
      name := "uri-template",
      scalaVersion := crossScalaVersions.value.head,
      crossScalaVersions := Seq("2.12.13", "2.13.4"),
      publishTo := {
        if (isSnapshot.value) {
          Some(Opts.resolver.sonatypeSnapshots)
        } else {
          Some(Opts.resolver.sonatypeStaging)
        }
      },
      pomIncludeRepository := { x => false },
      credentials += Credentials(Path.userHome / ".sbt" / "arktekk-credentials"),
      libraryDependencies ++= Seq(
        "org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.2",
        "org.scalatest"          %% "scalatest"                % "3.2.3" % "test"
      ),
      releaseCrossBuild := true,
      releasePublishArtifactsAction := PgpKeys.publishSigned.value,
      homepage := Some(new URL("http://github.com/arktekk/uri-template")),
      startYear := Some(2011),
      licenses := Seq(("Apache 2", new URL("http://www.apache.org/licenses/LICENSE-2.0.txt"))),
      scmInfo := Some(
        ScmInfo(
          browseUrl = new URL("https://github.com/arktekk/uri-template"),
          connection = "scm:git:git://github.com/arktekk/uri-template.git",
          devConnection = Some("scm:git:git@github.com:arktekk/uri-template.git")
        )
      ),
      developers += Developer(
        id = "teigen",
        name = "Jon-Anders Teigen",
        email = "",
         url = new URL("http://twitter.com/jteigen")
      )
    )
  )
