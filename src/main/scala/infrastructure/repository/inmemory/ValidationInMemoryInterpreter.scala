package infrastructure.repository.inmemory

import cats.Applicative
import domain.{PlantAlreadyExists, PlantDoesNotExist, PlantId, ValidationAlgebra}
import cats.data.EitherT.leftT

class ValidationInMemoryInterpreter[F[_]: Applicative] extends ValidationAlgebra[F] {

  def checkPlantDoesNotExist(name: String): F[Either[PlantAlreadyExists.type, Unit]] =
    leftT(PlantAlreadyExists).value

  def checkPlantExists(id: PlantId): F[Either[PlantDoesNotExist.type, Unit]] =
    leftT(PlantDoesNotExist).value

}

object ValidationInMemoryInterpreter {
  def apply[F[_]: Applicative] = new ValidationInMemoryInterpreter[F]
}