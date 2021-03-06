package org.dedkot.bot

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object Main extends App {
  val bot = new CryptoBot("1677104065:AAEBl1mat_9nqffcLEX8vLKJ0v3LpZm_OUo")
  val eol = bot.run
  println("Press [ENTER] to shutdown the bot, it may take a few seconds...")
  scala.io.StdIn.readLine
  bot.shutdown // initiate shutdown
  // Wait for the bot end-of-life
  Await.result(eol, Duration.Inf)
}
