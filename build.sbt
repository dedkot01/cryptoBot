name := "crypto-bot"

version := "0.1"

lazy val bot = (project in file("bot"))
  .dependsOn(generator)
  .settings(
    assemblyJarName in assembly := "cryptoBot.jar",
    libraryDependencies ++= Dependencies.tgBot
      ++ Dependencies.akka
      ++ Dependencies.doobie
  )

lazy val generator = (project in file("generator-data"))
  .settings(
    assemblyJarName in assembly := "generatorCandle.jar",
    libraryDependencies ++= Dependencies.doobie
  )

lazy val root = (project in file("."))
  .aggregate(bot, generator)
