package domain

import java.util.UUID

import cats.effect.Async
import domain.events.RawEvent
import fs2.Stream

trait EventLogAlgebra [F[_]] {

  def generateUID()(implicit async: Async[F]): F[UUID] // Currently put the application id generation at this algebra

  def append(e: RawEvent): F[RawEvent]

  def consume(): Stream[F, RawEvent]
}
