package org.dedkot.generator.models

import org.dedkot.generator.models.common.Figi

import java.time.Instant
import scala.concurrent.duration.FiniteDuration

case class Candle(interval: FiniteDuration, figi: common.Figi, details: CandleDetails)

case class CandleDetails(low: BigDecimal, high: BigDecimal, open: BigDecimal, close: BigDecimal, openTime: Instant)

object Figis {

  val BTC: Figi = Figi("BTC")
  val ETH: Figi = Figi("ETH")
  val BNB: Figi = Figi("BNB")
  val DOGE: Figi = Figi("DOGE")
  val DOT: Figi = Figi("DOT")
  val ADA: Figi = Figi("ADA")

}

object common {

  case class Figi(value: String)

}
