package org.dedkot.generator

import org.dedkot.generator.models.{Candle, CandleDetails, CandleRepository, Figis}

import java.time.Instant
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration
import scala.util.Random

object Main extends App {
  while(true) {
    val candle = Candle(
      FiniteDuration(5000, TimeUnit.MILLISECONDS),
      Random.nextInt(6) match {
        case 0 => Figis.ADA
        case 1 => Figis.BNB
        case 2 => Figis.BTC
        case 3 => Figis.DOGE
        case 4 => Figis.DOT
        case 5 => Figis.ETH
      },
      CandleDetails(Random.nextDouble() * 100, Random.nextDouble() * 100,
        Random.nextDouble() * 100, Random.nextDouble() * 100, Instant.now())
    )

    CandleRepository.upsertCandle(candle)

    println(candle)
    Thread.sleep(1000)
  }
}
