package org.dedkot.bot

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import cats.instances.future._
import cats.syntax.functor._
import com.bot4s.telegram.Implicits._
import com.bot4s.telegram.api.declarative.{Callbacks, Commands}
import com.bot4s.telegram.future.Polling
import com.bot4s.telegram.methods.{EditMessageReplyMarkup, EditMessageText, SendMessage}
import com.bot4s.telegram.models.{ChatId, InlineKeyboardButton, InlineKeyboardMarkup, KeyboardButton, ReplyKeyboardMarkup}
import org.dedkot.generator.models.CandleRepository

import scala.concurrent.Future
import scala.util.Try

class CryptoBot(token: String) extends ExampleBot(token)
  with Polling
  with Commands[Future]
  with Callbacks[Future] {

  private val cryptoKeyboard = ReplyKeyboardMarkup(
    keyboard = Seq(
      Seq(KeyboardButton("/ADA"), KeyboardButton("/BNB")),
      Seq(KeyboardButton("/BTC"), KeyboardButton("/DOGE")),
      Seq(KeyboardButton("/DOT"), KeyboardButton("/ETH")),
    ),
    resizeKeyboard = Option(true)
  )

  private val updateBtn = InlineKeyboardMarkup.singleButton(
    InlineKeyboardButton("Обновить", callbackData = Option("Update"))
  )

  onCommand("/start") { implicit msg =>
    reply("Выберите криптовалюту", replyMarkup = Option(cryptoKeyboard)).void
  }

  onCommand("ADA" | "BNB" | "BTC" | "DOGE" | "DOT" | "ETH") { implicit msg =>
    val candle = CandleRepository.selectCandle(msg.text.get.split("/")(1))
    val answer =
      s"""
        |${candle.figi.value}:
        |* Интервал: ${candle.interval}
        |* Нижняя граница: ${candle.details.low}
        |* Верхняя граница: ${candle.details.high}
        |* Открытие: ${candle.details.open}
        |* Закрытие: ${candle.details.close}
        |* Время: ${candle.details.openTime}
        |""".stripMargin
    reply(answer, replyMarkup = Option(updateBtn)).void
  }

  onCallbackQuery { implicit cbq =>
    val candle = CandleRepository.selectCandle(cbq.message.get.text.get.split(":")(0))
    val answer = EditMessageText(
      ChatId(cbq.message.get.source),
      cbq.message.get.messageId,
      text = s"""
                |${candle.figi.value}:
                |* Интервал: ${candle.interval}
                |* Нижняя граница: ${candle.details.low}
                |* Верхняя граница: ${candle.details.high}
                |* Открытие: ${candle.details.open}
                |* Закрытие: ${candle.details.close}
                |* Время: ${candle.details.openTime}
                |""".stripMargin,
      replyMarkup = updateBtn
    )

    ackCallback().zip(request(answer).getOrElse(Future.successful(()))).void
  }

}
