package org.dedkot.bot

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import cats.instances.future._
import cats.syntax.functor._
import com.bot4s.telegram.Implicits._
import com.bot4s.telegram.api.declarative.{Callbacks, Commands}
import com.bot4s.telegram.future.Polling
import com.bot4s.telegram.methods.{EditMessageText, SendMessage}
import com.bot4s.telegram.models._
import org.dedkot.generator.models.{Candle, CandleRepository}

import scala.concurrent.Future

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

  private val actorSystem = ActorSystem("MasterOfSubscription")
  private val mapOfActors = collection.mutable.Map[Int, ActorRef]()

  onCommand("/start") { implicit msg =>
    reply("Выберите криптовалюту", replyMarkup = Option(cryptoKeyboard)).void
  }

  onCommand("ADA" | "BNB" | "BTC" | "DOGE" | "DOT" | "ETH") { implicit msg =>
    Future(
      actorSystem.actorOf(SubscriptionForUpdate.props(msg.text.get.split("/")(1), msg))
    )
  }

  onCallbackQuery { implicit cbq =>
    Future(
      mapOfActors(cbq.message.get.messageId) ! "STOP"
    )
  }

  class SubscriptionForUpdate(private val figi: String,
                              implicit private var msg: Message) extends Actor {

    private val sendMsg = SendMessage(
      msg.source,
      text = text(CandleRepository.selectCandle(figi)),
      replyMarkup = stopBtn
    )

    def receive: Receive = {
      case "STOP" =>
        context.stop(self)
        request(
          EditMessageText(
            Option(msg.source),
            Option(msg.messageId),
            text = msg.text.get
          )
        ).void
      case _ =>
        Thread.sleep(5000)
        request(
          EditMessageText(
            Option(msg.source),
            Option(msg.messageId),
            text = text(CandleRepository.selectCandle(figi)),
            replyMarkup = stopBtn
          )
        ).void
        context.self ! "UPDATE"
    }

    request(sendMsg).onComplete { sendMsg =>
      msg = sendMsg.get
      mapOfActors += (msg.messageId -> context.self)
      context.self ! "UPDATE"
    }

    private def text(candle: Candle) =
      s"""
         |${candle.figi.value}:
         |* Интервал: ${candle.interval}
         |* Нижняя граница: ${candle.details.low}
         |* Верхняя граница: ${candle.details.high}
         |* Открытие: ${candle.details.open}
         |* Закрытие: ${candle.details.close}
         |* Время: ${candle.details.openTime}
         |""".stripMargin

  }

  object SubscriptionForUpdate {

    def props(figi: String, msg: Message): Props = Props(new SubscriptionForUpdate(figi, msg))

  }

}
