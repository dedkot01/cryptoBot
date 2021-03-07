package org.dedkot.generator.models

import cats.effect._
import doobie._
import doobie.implicits._

import java.time.Instant
import java.util.concurrent.TimeUnit
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration

object CandleRepository {

  private implicit val cs = IO.contextShift(ExecutionContext.global)

  private val xa = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver",
    "jdbc:postgresql://localhost:5432/postgres",
    "postgres", // user
    "12345" // password
  )

  private val createTable =
    sql"""
        CREATE TABLE IF NOT EXISTS candle
        (
          figi character varying(100) NOT NULL,
          interval character varying(100),
          low character varying(100),
          high character varying(100),
          open character varying(100),
          close character varying(100),
          "openTime" bigint,
          PRIMARY KEY (figi)
        );
      """.update.run.transact(xa).unsafeRunSync

  def selectCandle(figi: String): Candle = {
    val result = sql"""
         SELECT interval, figi, low, high, open, close, "openTime" FROM candle WHERE figi = $figi;
       """
      .query[(String, String, Double, Double, Double, Double, Long)]
      .stream
      .head
      .compile.last
      .transact(xa)
      .unsafeRunSync
      .get

    Candle(
      FiniteDuration(result._1.split(" ")(0).toInt, TimeUnit.MILLISECONDS),
      result._2 match {
        case "ADA" => Figis.ADA
        case "BNB" => Figis.BNB
        case "BTC" => Figis.BTC
        case "DOGE" => Figis.DOGE
        case "DOT" => Figis.DOT
        case "ETH" => Figis.ETH
      },
      CandleDetails(result._3, result._4, result._5, result._6, Instant.ofEpochSecond(result._7))
    )
  }

  def upsertCandle(candle: Candle): Int =
    sql"""
        INSERT INTO candle (figi, interval, low, high, open, close, "openTime")
        VALUES (${candle.figi.value}, ${candle.interval.toString},
          ${candle.details.low}, ${candle.details.high},
          ${candle.details.open}, ${candle.details.close},
          ${candle.details.openTime.getEpochSecond})
        ON CONFLICT (figi)
        DO UPDATE SET interval = ${candle.interval.toString},
          low = ${candle.details.low},
          high = ${candle.details.high},
          open = ${candle.details.open},
          close = ${candle.details.close},
          "openTime" = ${candle.details.openTime.getEpochSecond};
      """.update.run.transact(xa).unsafeRunSync

}
