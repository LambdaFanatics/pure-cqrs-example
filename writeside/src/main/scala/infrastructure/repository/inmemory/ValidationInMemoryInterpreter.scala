package infrastructure.repository.inmemory

import cats.Applicative
import cats.implicits._
import domain._
import domain.validations._

// TODO implement actual validations
class ValidationInMemoryInterpreter[F[_]: Applicative] extends ValidationAlgebra[F] {

  def checkThatCarIsRegistered(regPlate: String): F[Either[validations.CarNotRegistered.type, Unit]] =
    ().asRight[CarNotRegistered.type].pure[F]

  def checkThatCarIsNotRegistered(regPlate: String): F[Either[CarAlreadyRegistered.type, Unit]] =
    ().asRight[CarAlreadyRegistered.type].pure[F]


  def checkThatPartIsMarked(regPlate: String, part: String): F[Either[validations.PartIsNotMarked.type, Unit]] =
    ().asRight[PartIsNotMarked.type].pure[F]

  def checkThatPartIsNotMarked(regPlate: String, part: String): F[Either[validations.PartIsAlreadyMarked.type, Unit]] =
    ().asRight[PartIsAlreadyMarked.type].pure[F]

  def checkThatCarHasNoDamages(regPlate: String): F[Either[validations.CarHasDamagedParts.type, Unit]] =
    ().asRight[CarHasDamagedParts.type].pure[F]
}

object ValidationInMemoryInterpreter {
  def apply[F[_]: Applicative] = new ValidationInMemoryInterpreter[F]
}