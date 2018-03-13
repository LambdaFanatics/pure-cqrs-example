package domain

import domain.events.Event
import fs2.Stream

trait EventLogAlgebra [F[_]] {

  def append(e: Event): F[Event]

  def consume(consumerName: String, from: LogOffset, closeOnEnd: Boolean): Stream[F, Event]
}

sealed trait LogOffset
case object SeekBeginning extends LogOffset
case object SeekEnd extends LogOffset