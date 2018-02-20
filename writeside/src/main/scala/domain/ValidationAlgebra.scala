package domain

import validations._

trait ValidationAlgebra[F[_]] {

  type PlantDescription = (PlantId, String)

  //FIXME: Awfull implementation and API reimplement
  def put(p: (PlantId, String)): F[Unit]
  def delete(id: PlantId): F[Unit]

  def checkPlantDoesNotExist(name: String): F[Either[PlantAlreadyExists.type, Unit]]
  def checkPlantExists(id: PlantId): F[Either[PlantDoesNotExist.type, PlantDescription]]
}
