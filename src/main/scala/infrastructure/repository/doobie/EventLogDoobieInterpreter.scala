package infrastructure.repository.doobie

import java.util.UUID

import cats.Monad
import cats.effect.Async
import domain.{RawEvent, EventLogAlgebra}
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.util.transactor.Transactor
import fs2.Stream

class EventLogDoobieInterpreter[F[_]: Monad](val xa: Transactor[F]) extends EventLogAlgebra[F] {
  private object queries {

    def append(event: RawEvent): ConnectionIO[RawEvent] = {
      sql"""INSERT INTO events (payload) VALUES (${event.payload})"""
        .update
        .withUniqueGeneratedKeys[Long]("id")
        .map(id => event.copy(id = Some(id)) )
    }


    val streamAll: Stream[ConnectionIO, RawEvent] =
      sql"""select id, payload from events""".query[RawEvent].process

  }

  def append(e: RawEvent): F[RawEvent] = queries.append(e).transact(xa)

  def consume(): fs2.Stream[F, RawEvent] = queries.streamAll.transact(xa)

  def generateUID()(implicit async: Async[F]): F[UUID] = async.delay(UUID.randomUUID())
}

object EventLogDoobieInterpreter {
  def apply[F[_] : Monad](xa: Transactor[F]) = new EventLogDoobieInterpreter(xa)
}
