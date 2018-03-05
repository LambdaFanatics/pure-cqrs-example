package domain

import cats.Monad
import cats.data.OptionT
import domain.cars.{Car, Repaired}

trait CarStoreAlgebra[F[_]] {
  def create(car: Car): F[Car]

  def update(car: Car): F[Car]

  def get(regPlate: String): F[Option[Car]]

  def list(): F[List[Car]]

  def repairCar(regPlate: String)(implicit F: Monad[F]): F[Option[Car]] = {
    val res = for {
      car <- OptionT(get(regPlate))
      _ <- OptionT.liftF(update(car.copy(status = Repaired)))
    } yield car
    res.value
  }

}
