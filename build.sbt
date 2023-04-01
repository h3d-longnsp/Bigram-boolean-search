ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

lazy val root = (project in file("."))
  .settings(
    name := "vscodeA4"
  )

libraryDependencies ++= Seq(
  "com.github.rholder" % "snowball-stemmer" % "1.3.0.581.1",
  "org.scala-lang.modules" %% "scala-parallel-collections" % "1.0.4"
)

mainClass in assembly := Some("org.long.A4.GUI")