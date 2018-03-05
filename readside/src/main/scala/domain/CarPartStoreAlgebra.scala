package domain

import domain.cars.CarPart

trait CarPartStoreAlgebra[F[_]] {
  def create(part: CarPart): F[CarPart]

  def update(part: CarPart): F[CarPart]

  def findByNameAndPlate(name: String, carPlate: String): F[Option[CarPart]]

  def list(): F[List[CarPart]]

  def markPart(name: )

}
