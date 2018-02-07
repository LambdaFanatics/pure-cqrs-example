package infrastructure.repository.doobie

import cats.Monad
import domain.{Event, EventLogAlgebra}
import doobie.free.connection.ConnectionIO
import doobie.util.transactor.Transactor

import doobie.implicits._

class EventLogDoobieInterpreter[F[_]: Monad](val xa: Transactor[F]) extends EventLogAlgebra[F] {
  private object queries {

    def append(event: Event): ConnectionIO[Event] = {
      sql"""INSERT INTO events (payload) VALUES (${event.payload})"""
        .update
        .withUniqueGeneratedKeys[Long]("id")
        .map(id => event.copy(id = Some(id)) )
    }



  }

  def append(e: Event): F[Event] = queries.append(e).transact(xa)
}

object EventLogDoobieInterpreter {
  def apply[F[_] : Monad](xa: Transactor[F]) = new EventLogDoobieInterpreter(xa)
}
