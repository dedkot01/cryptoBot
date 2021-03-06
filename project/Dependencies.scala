import sbt._

object Dependencies {

  lazy val tgBot = Seq(
    "com.bot4s" %% "telegram-core" % versions.tgBot,
    "com.bot4s" %% "telegram-akka" % versions.tgBot
  )

  lazy val akka = Seq(
    "com.typesafe.akka" %% "akka-actor-typed" % versions.akka,
    "ch.qos.logback" % "logback-classic" % "1.2.3"
  )

  lazy val doobie = Seq(
    "org.tpolecat" %% "doobie-core"     % versions.doobie,
    "org.tpolecat" %% "doobie-postgres" % versions.doobie,
    "org.tpolecat" %% "doobie-specs2"   % versions.doobie
  )

  object versions {

    val tgBot = "4.4.0-RC2"
    val akka = "2.6.12"
    val doobie = "0.9.0"

  }

}
