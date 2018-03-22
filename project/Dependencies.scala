import sbt._

object Dependencies {

  object Versions {
    val catsVersion = "1.1.0"
    val catsMtlVersion = "0.2.1"
    val doobieVersion = "0.5.0"
    val flywayVersion = "5.0.0"
    val logbackVersion = "1.2.3"
    val pureConfigVersion = "0.9.0"
    val circeVersion = "0.9.1"
    val fs2Version = "0.10.3"
    val http4sVersion = "0.18.0"
    val scalaTest   = "3.0.3"
    val scalaCheck  = "1.13.4"
  }

  object Libraries {

    import Versions._

    // FP related
    lazy val catsCore = "org.typelevel" %% "cats-core" % catsVersion

    // Database related
    lazy val doobieCore = "org.tpolecat" %% "doobie-core" % doobieVersion
    lazy val doobiePostgres = "org.tpolecat" %% "doobie-postgres" % doobieVersion
    lazy val doobieHikari = "org.tpolecat" %% "doobie-hikari" % doobieVersion

    lazy val flyway = "org.flywaydb" % "flyway-core" % flywayVersion

    // Logging
    lazy val logback = "ch.qos.logback" % "logback-classic" % logbackVersion

    // Configuration
    lazy val pureConfig = "com.github.pureconfig" %% "pureconfig" % pureConfigVersion

    // JSON Encoding
    lazy val circeGeneric = "io.circe" %% "circe-generic" % circeVersion
    lazy val circeLiteral = "io.circe" %% "circe-literal" % circeVersion
    lazy val circeGenericExtras = "io.circe" %% "circe-generic-extras" % circeVersion
    lazy val circeOptics = "io.circe" %% "circe-optics" % circeVersion
    lazy val circeParser = "io.circe" %% "circe-parser" % circeVersion
    lazy val circeJava8 = "io.circe" %% "circe-java8" % circeVersion

    // Streaming
    lazy val fs2Core = "co.fs2" %% "fs2-core" % fs2Version

    // Rest / ws api (the servers)
    lazy val http4sServer = "org.http4s" %% "http4s-blaze-server" % http4sVersion
    lazy val http4sCirce = "org.http4s" %% "http4s-circe" % http4sVersion
    lazy val http4sDsl = "org.http4s" %% "http4s-dsl" % http4sVersion

    // Testing
    lazy val scalaTest = "org.scalatest" %% "scalatest" % Versions.scalaTest % Test
    lazy val scalaCheck = "org.scalacheck" %% "scalacheck" % Versions.scalaCheck % Test
    lazy val doobieTest = "org.tpolecat" %% "doobie-scalatest" % doobieVersion % Test

    // Bundles
    lazy val catsBundle = Seq(catsCore)
    lazy val doobieBundle = Seq(doobieCore, doobieHikari, doobiePostgres, doobieTest)
    lazy val circeBundle = Seq(circeGeneric, circeLiteral, circeGenericExtras, circeOptics, circeParser, circeJava8)
    lazy val http4sBundle = Seq(http4sServer, http4sCirce, http4sDsl)
    lazy val testingBundle = Seq(scalaTest, scalaCheck)
  }
}
