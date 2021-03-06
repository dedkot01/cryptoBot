package org.dedkot.bot

import akka.NotUsed
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorSystem, Behavior}
import cats.instances.future._
import cats.syntax.functor._
import com.bot4s.telegram.Implicits._
import com.bot4s.telegram.api.declarative.{Callbacks, Commands}
import com.bot4s.telegram.future.Polling
import com.bot4s.telegram.methods.{EditMessageReplyMarkup, EditMessageText}
import com.bot4s.telegram.models.{ChatId, InlineKeyboardButton, InlineKeyboardMarkup, KeyboardButton, ReplyKeyboardMarkup}

import scala.concurrent.Future
import scala.util.Try

class CryptoBot(token: String) extends ExampleBot(token)
  with Polling
  with Commands[Future]
  with Callbacks[Future] {

  val cryptoKeyboard = ReplyKeyboardMarkup(
    keyboard = Seq(
      Seq(KeyboardButton("ADA"), KeyboardButton("BNB")),
      Seq(KeyboardButton("BTC"), KeyboardButton("DOGE")),
      Seq(KeyboardButton("DOT"), KeyboardButton("ETH")),
    ),
    resizeKeyboard = Option(true)
  )

  val stopBtn = InlineKeyboardMarkup.singleButton(
    InlineKeyboardButton("СТОП", callbackData = Option("Stop"))
  )

  onCommand("/start") { implicit msg =>
    reply("Выберите криптовалюту", replyMarkup = Option(cryptoKeyboard)).void
  }

  onCommand("/inline") { implicit msg =>
    reply("Держи инлайн кнопку", replyMarkup = Option(stopBtn)).void
  }

  onCallbackQuery { implicit cbq =>
    val answer = EditMessageText(
      ChatId(cbq.message.get.source), // msg.chat.id
      cbq.message.get.messageId,
      text = "красава")

    ackCallback().zip(request(answer).getOrElse(Future.successful(()))).void
  }

}