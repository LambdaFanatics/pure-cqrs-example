package domain

import domain.cars.Car

trait CarStoreAlgebra[F[_]] {
  def create(car: Car): F[Car]

  def update(car: Car): F[Option[Car]]

  def modify(regPlate: String)(f: Car => Car): F[Option[Car]]

  def get(regPlate: String): F[Option[Car]]

  def list(): F[List[Car]]
}
