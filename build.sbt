name := """cryptofintechx"""
organization := "com.shiftio"

version := "1.0"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.6"

resolvers += Resolver.mavenLocal
resolvers += Resolver.bintrayRepo("mattmoore", "bcrypt-scala")

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test
libraryDependencies += "com.typesafe.play" %% "play-slick" % "5.0.0"
libraryDependencies += "com.typesafe.play" %% "play-slick-evolutions" % "5.0.0"
libraryDependencies += "mysql" % "mysql-connector-java" % "8.0.25"
libraryDependencies += "com.github.t3hnar" % "scala-bcrypt_2.12" % "4.3.0"
libraryDependencies += "com.github.daddykotex" %% "courier" % "3.0.1"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % "0.14.1")

libraryDependencies ++= Seq(
  "com.pauldijou" %% "jwt-play" % "5.0.0",
  "com.pauldijou" %% "jwt-core" % "5.0.0",
  "com.auth0" % "jwks-rsa" % "0.20.0"
)