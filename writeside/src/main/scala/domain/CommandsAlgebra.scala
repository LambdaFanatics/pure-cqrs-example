package domain

import java.util.UUID

import validations._
import events._

trait CommandsAlgebra[F[_]] {
  def create(name: String, country: String): F[Either[ValidationError, Event]]
  def delete(id: UUID): F[Either[ValidationError,Event]]
}
