package org.dedkot.generator.models

import doobie._
import doobie.implicits._
import cats.effect._

import scala.concurrent.ExecutionContext

object CandleRepository {

  implicit val cs = IO.contextShift(ExecutionContext.global)

  val xa = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver",
    "jdbc:postgresql://localhost:5432/postgres",
    "postgres", // user
    "12345" // password
  )

  val createTable =
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

  def upsertCandle(candle: Candle): Int =
    sql"""
        INSERT INTO candle (figi, interval, low, high, open, close, "openTime")
        VALUES (${candle.figi.value}, ${candle.interval.toString},
          ${candle.details.low}, ${candle.details.high},
          ${candle.details.open}, ${candle.details.close},
          ${candle.details.openTime.getEpochSecond * 1000})
        ON CONFLICT (figi)
        DO UPDATE SET interval = ${candle.interval.toString},
          low = ${candle.details.low},
          high = ${candle.details.high},
          open = ${candle.details.open},
          close = ${candle.details.close},
          "openTime" = ${candle.details.openTime.getEpochSecond * 1000};
      """.update.run.transact(xa).unsafeRunSync

}
