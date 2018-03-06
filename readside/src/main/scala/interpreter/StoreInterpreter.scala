package interpreter

import cats.Monad
import cats.implicits._
import domain._
import domain.cars.Car
import domain.parts.CarPart

class StoreInterpreter[F[_] : Monad](carStore: CarStoreAlgebra[F], partStore: CarPartStoreAlgebra[F]) extends StoreAlgebra[F] {

  def registerCar(regPlate: String, model: String): F[Car] =
    carStore.create(Car(regPlate, model))

  def repairCar(regPlate: String): F[Option[Car]] =
    carStore.modify(regPlate)(car => car.copy(status = cars.Repaired))


  def markPart(regPlate: String, name: String): F[Option[CarPart]] = for {
    // This is an upsert (= insert or update)
    maybeUpdated <- partStore.modify(regPlate, name) { part => part.copy(status = parts.Damaged) }
    result <- maybeUpdated match {
      case part:Some[CarPart] => part.pure[F]
      case None => partStore.create(CarPart(name, regPlate, status = parts.Damaged)).map(_.some)
    }
  } yield result


  def unmarkPart(regPlate: String, name: String): F[Option[CarPart]] =
    partStore.delete(name, regPlate)

  def repairPart(regPlate: String, name: String): F[Option[CarPart]] =
    partStore.modify(regPlate, name) { part => part.copy(status = parts.Repaired) }

}
