package domain

import domain.cars.DamagedCar

trait DamagedCarStoreAlgebra[F[_]] {
  def create(car: DamagedCar): F[DamagedCar]

  def update(car: DamagedCar): F[DamagedCar]

  def list(): F[List[DamagedCar]]

}
