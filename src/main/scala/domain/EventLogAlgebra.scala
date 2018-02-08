package domain

import fs2.Stream

trait EventLogAlgebra [F[_]] {

  def append(e: Event): F[Event]

  def consume(): Stream[F, Event]
}
