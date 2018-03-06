package domain

import domain.cars.Car
import domain.parts.CarPart


trait StoreAlgebra[F[_]] {
  def registerCar(regPlate: String, model: String): F[Car]

  def repairCar(regPlate: String): F[Option[Car]]

  def markPart(regPlate: String, name: String): F[Option[CarPart]]

  def unmarkPart(regPlate: String, name: String): F[Option[CarPart]]

  def repairPart(regPlate: String, name: String): F[Option[CarPart]]
}
