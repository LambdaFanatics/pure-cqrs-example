package domain

trait EventLogAlgebra [F[_]] {
  def append(e: Event): F[Event]
}
