package domain

import validations._
import events._

trait CommandsAlgebra[F[_]] {
  def create(name: String, country: String): F[Either[ValidationError, RawEvent]]
  def delete(id: PlantId): F[Either[ValidationError,RawEvent]]
}
