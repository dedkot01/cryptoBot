package org.dedkot.bot

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import cats.instances.future._
import cats.syntax.functor._
import com.bot4s.telegram.Implicits._
import com.bot4s.telegram.api.declarative.{Callbacks, Commands}
import com.bot4s.telegram.future.Polling
import com.bot4s.telegram.methods.{EditMessageReplyMarkup, EditMessageText, SendMessage}
import com.bot4s.telegram.models.{ChatId, InlineKeyboardButton, InlineKeyboardMarkup, KeyboardButton, Message, ReplyKeyboardMarkup}
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

  private val stopBtn = InlineKeyboardMarkup.singleButton(
    InlineKeyboardButton("СТОП", callbackData = Option("Stop"))
  )

  onCommand("/start") { implicit msg =>
    reply("Выберите криптовалюту", replyMarkup = Option(cryptoKeyboard)).void
  }

  private val mapOfActors = collection.mutable.Map[Int, ActorRef]()

  object SubscriptionForUpdate {

    def props(figi: String, msg: Message): Props = Props(new SubscriptionForUpdate(figi, msg))

  }

  class SubscriptionForUpdate(figi: String, implicit val msg: Message) extends Actor {

    var candle = CandleRepository.selectCandle(figi)
    val answer = SendMessage(
      msg.source,
      text =
        s"""
           |${candle.figi.value}:
           |* Интервал: ${candle.interval}
           |* Нижняя граница: ${candle.details.low}
           |* Верхняя граница: ${candle.details.high}
           |* Открытие: ${candle.details.open}
           |* Закрытие: ${candle.details.close}
           |* Время: ${candle.details.openTime}
           |""".stripMargin,
      replyMarkup = stopBtn
    )

    var myMsg: Message = null

    request(answer).get map { msg => myMsg = msg }
    Thread.sleep(1000)
    mapOfActors += (myMsg.messageId -> context.self)
    context.self ! "UPDATE"

    def receive = {
      case "STOP" =>
        context.stop(self)
        val answerE = EditMessageText(
          Option(myMsg.source),
          Option(myMsg.messageId),
          text =
            s"""
               |${candle.figi.value}:
               |* Интервал: ${candle.interval}
               |* Нижняя граница: ${candle.details.low}
               |* Верхняя граница: ${candle.details.high}
               |* Открытие: ${candle.details.open}
               |* Закрытие: ${candle.details.close}
               |* Время: ${candle.details.openTime}
               |""".stripMargin
        )
        request(answerE).void
      case _ =>
        Thread.sleep(5000)
        println("Вроде ходют")
        candle = CandleRepository.selectCandle(figi)
        val answerE = EditMessageText(
          Option(myMsg.source),
          Option(myMsg.messageId),
          text =
            s"""
              |${candle.figi.value}:
              |* Интервал: ${candle.interval}
              |* Нижняя граница: ${candle.details.low}
              |* Верхняя граница: ${candle.details.high}
              |* Открытие: ${candle.details.open}
              |* Закрытие: ${candle.details.close}
              |* Время: ${candle.details.openTime}
              |""".stripMargin,
          replyMarkup = stopBtn
        )
        request(answerE).void
        context.self ! "UPDATE"
    }

  }

  val system = ActorSystem("MasterOfSubscription")

  onCommand("ADA" | "BNB" | "BTC" | "DOGE" | "DOT" | "ETH") { implicit msg =>
    system.actorOf(SubscriptionForUpdate.props(msg.text.get.split("/")(1), msg))
    Future[Unit]()
  }

  onCallbackQuery { implicit cbq =>
    mapOfActors(cbq.message.get.messageId) ! "STOP"
    Future[Unit]()
  }

}
