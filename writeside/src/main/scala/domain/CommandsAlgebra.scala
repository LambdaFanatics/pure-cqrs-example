package domain

import java.util.UUID

import validations._
import events._

trait CommandsAlgebra[F[_]] {
  def registerCar(regPlate: String, model: String): F[Either[ValidationError, Event]]
  def repairCar(id: UUID): F[Either[ValidationError, Event]]
  def addDamagedPart(carId: UUID, name: String): F[Either[ValidationError,Event]]
  def removeDamagedPart(id: UUID): F[Either[ValidationError, Event]]
  def repairDamagedPart(id: UUID): F[Either[ValidationError, Event]]

}
