package service

import cats.{Monad, ~>}
import domain.StoreAlgebra
import domain.cars.Car
import domain.parts.CarPart
import utils.functional._

class CarService[G[_]: Monad, F[_]](store: StoreAlgebra[G])(implicit exec: G ~> F) {

  def getCars: F[List[(Car, List[CarPart])]] = store.fetchCarsWithParts().liftTo[F]

}
