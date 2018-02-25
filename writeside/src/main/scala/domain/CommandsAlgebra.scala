package domain

import cats.data.EitherT
import domain.events._
import domain.validations._

trait CommandsAlgebra[F[_]] {
  def registerCar(regPlate: String, model: String): EitherT[F, ValidationError, Event]

  def repairCar(regPlate: String): EitherT[F, ValidationError, Event]

  def markPart(regPlate: String, part: String): EitherT[F, ValidationError, Event]

  def unmarkPart(regPlate: String, part: String): EitherT[F, ValidationError, Event]

  def repairPart(regPlate: String, part: String): EitherT[F, ValidationError, Event]
}
