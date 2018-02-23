package domain

import java.util.UUID

import domain.events.Event
import fs2.Stream

trait EventLogAlgebra [F[_]] {

  def generateUID(): F[UUID] // Currently put the application id generation at this algebra

  def append(e: Event): F[Event]

  def consume(): Stream[F, Event]
}
