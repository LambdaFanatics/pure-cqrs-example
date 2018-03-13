package interpreter.doobie


import cats.effect.Effect
import cats.implicits._
import domain.events.Event
import domain.events.codec._
import domain.{EventLogAlgebra, LogOffset, SeekBeginning}
import doobie._
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.util.transactor.Transactor
import fs2.{Pipe, Stream}
import io.circe.Json
import io.circe.parser._
import io.circe.syntax._
import org.postgresql.util.PGobject


class EventLogDoobieInterpreter[F[_] : Effect](val xa: Transactor[F]) extends EventLogAlgebra[F] {

  case class EventRow(id: Option[Long], payload: Json)

  case class OffsetRow(consumerName: String, offset: Long)

  private object queries {

    implicit val JsonMeta: Meta[Json] =
      Meta.other[PGobject]("json").xmap[Json](
        a => parse(a.getValue).leftMap[Json](e => throw e).merge,
        a => {
          val o = new PGobject
          o.setType("json")
          o.setValue(a.noSpaces)
          o
        }
      )

    def append(event: EventRow): ConnectionIO[EventRow] = {
      sql"""INSERT INTO events (payload) VALUES (${event.payload})"""
        .update
        .withUniqueGeneratedKeys[Long]("id")
        .map(id => event.copy(id = Some(id)))
    }


    def streamFrom(offset: Long): Stream[ConnectionIO, EventRow] =
      sql"""SELECT id, payload FROM events WHERE id > $offset ORDER BY id ASC""".query[EventRow].stream

    def selectOffset(consumerName: String): Query0[OffsetRow] =
      sql"""
        SELECT consumer_name, log_offset
        FROM event_consumers
        WHERE consumer_name = $consumerName
      """.query

    def insertOffset(consumerName: String, offset: Long): Update0 =
      sql"""
      INSERT INTO event_consumers (consumer_name, log_offset)
      VALUES  ($consumerName, $offset)
    """.update

    def updateOffset(consumerName: String, offset: Long): Update0 =
      sql"""
      UPDATE event_consumers
      SET log_offset = $offset
      WHERE consumer_name = $consumerName
    """.update

  }

  def append(e: Event): F[Event] = queries.append(EventRow(None, e.asJson)).as(e).transact(xa)

  def consume(consumerName: String, from: LogOffset, closeOnEnd: Boolean): Stream[F, Event] = {
    import utils.stream._

    import scala.concurrent.ExecutionContext.Implicits.global
    import scala.concurrent.duration._

    val stream = consumeEvents(consumerName, from)
      .afterLastElement { optEvt =>
        optEvt.fold(().pure[F])(eventRow => storeOffset(consumerName, eventRow.id.getOrElse(0L)) transact xa)
      }
      .through(translateToEvent)

    if (closeOnEnd)
      stream
    else
      stream.repeatWithInterval(1.second)
  }

  private def consumeEvents(consumerName: String, offset: LogOffset): Stream[F, EventRow] =
    (for {
      maybeOffset <- consumerOffset(consumerName, offset)
      es <- queries.streamFrom(maybeOffset.getOrElse(0))
    } yield es) transact xa


  private def translateToEvent: Pipe[F, EventRow, Event] = s => s.map(_.payload.as[Event])
      .collect { case Right(ev) => ev } // Here we ignore the json parse errors


  private def consumerOffset(consumerName: String, from: LogOffset): Stream[ConnectionIO, Option[Long]] = {
    val offset: ConnectionIO[Option[Long]] =
      if (from == SeekBeginning)
        0L.some.pure[ConnectionIO]
      else
        getOffset(consumerName)

    Stream.eval(offset)
  }

    private def storeOffset(consumerName: String, offset: Long): ConnectionIO[Unit] = for {
      maybeOffset <- getOffset(consumerName)
      _ <- maybeOffset.fold(queries.insertOffset(consumerName, offset).run)(_ => queries.updateOffset(consumerName, offset).run)
    } yield ()


  private def getOffset(consumerName: String): ConnectionIO[Option[Long]] =
    queries.selectOffset(consumerName)
      .option
      .map(_.map(_.offset))
}

object EventLogDoobieInterpreter {
  def apply[F[_] : Effect](xa: Transactor[F]) = new EventLogDoobieInterpreter(xa)
}
