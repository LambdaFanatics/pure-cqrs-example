package infrastructure.repository.doobie

import java.util.UUID

import cats.effect.Sync
import cats.implicits._
import domain.EventLogAlgebra
import domain.events.Event
import domain.events.codec._
import doobie._
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.util.transactor.Transactor
import fs2.Stream
import io.circe.Json
import io.circe.parser._
import io.circe.syntax._
import org.postgresql.util.PGobject

case class RawEvent(id: Option[Long], payload: Json)

class EventLogDoobieInterpreter[F[_]: Sync](val xa: Transactor[F]) extends EventLogAlgebra[F] {

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

  private object queries {

    def append(event: RawEvent): ConnectionIO[RawEvent] = {
      sql"""INSERT INTO events (payload) VALUES (${event.payload})"""
        .update
        .withUniqueGeneratedKeys[Long]("id")
        .map(id => event.copy(id = Some(id)) )
    }


    val streamAll: Stream[ConnectionIO, RawEvent] =
      sql"""select id, payload from events""".query[RawEvent].stream

  }

  def append(e: Event): F[Event] = queries.append(RawEvent(None, e.asJson)).transact(xa).as(e)

  def consume(): fs2.Stream[F, Event] = queries.streamAll.transact(xa)
      .map(_.payload.as[Event])           // Here we ignore the json parse errors
      .collect { case Right(ev) => ev }

  def generateUID(): F[UUID] = Sync[F].delay(UUID.randomUUID())
}

object EventLogDoobieInterpreter {
  def apply[F[_] : Sync](xa: Transactor[F]) = new EventLogDoobieInterpreter(xa)
}
