package domain

trait ValidationAlgebra[F[_]] {
  def checkPlantDoesNotExist(name: String): F[Either[PlantAlreadyExists.type, Unit]]
  def checkPlantExists(id: PlantId): F[Either[PlantDoesNotExist.type, Unit]]
}
