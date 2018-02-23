package domain

import java.util.UUID

import validations._

trait ValidationAlgebra[F[_]] {

  type PlantDescription = (UUID, String)

  //FIXME: Awfull implementation and API reimplement
  def put(p: (UUID, String)): F[Unit]
  def delete(id: UUID): F[Unit]

  def checkPlantDoesNotExist(name: String): F[Either[PlantAlreadyExists.type, Unit]]
  def checkPlantExists(id: UUID): F[Either[PlantDoesNotExist.type, PlantDescription]]
}
