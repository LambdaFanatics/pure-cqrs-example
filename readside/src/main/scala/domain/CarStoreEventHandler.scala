package domain

import cats.effect.Effect
import cats.implicits._
import cats.{Monad, ~>}
import domain.cars.Car
import domain.events._

class CarStoreEventHandler[G[_] : Monad, F[_] : Effect](store: CarStoreAlgebra[G], elog: EventLogAlgebra[F])
                                                       (implicit trans: G ~> F) {

  import utils.functional._

  def updateStore(ev: Event): F[Unit] = ev match {
    case CarRegistered(plate, model) => store.create(Car(plate, model)).as(()).liftTo[F]

    case CarRepaired(plate) => store.repairCar(plate).as(()).liftTo[F]

    case PartMarked(plate, part) =>  ???

    //    case PartUnmarked(plate, part) => ???
    //    case PartRepaired(plate, part) => ???
    case _ => Effect[F].unit
  }
}

object CarStoreEventHandler {
  def apply[G[_] : Monad, F[_] : Effect](s: CarStoreAlgebra[G], elog: EventLogAlgebra[F])
                                        (implicit trans: G ~> F) = new CarStoreEventHandler(s, elog)
}
