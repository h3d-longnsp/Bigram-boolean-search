ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

lazy val root = (project in file("."))
  .settings(
    name := "vscodeA4"
  )

libraryDependencies += "com.github.rholder" % "snowball-stemmer" % "1.3.0.581.1"