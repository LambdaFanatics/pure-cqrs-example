package domain

import cats.effect.Effect
import cats.{Monad, ~>}
import domain.events._
import cats.implicits._
import utils.functional._
import fs2._
import utils.stream._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class CarsStoreEventHandler[G[_] : Monad, F[_] : Effect](store: StoreAlgebra[G], elog: EventLogAlgebra[F])
                                                        (implicit trans: G ~> F, ec: ExecutionContext) {


  def process(): Stream[F, Unit] = loopWithInterval(elog.consume().evalMap(updateStore), 10.second)


  def updateStore(ev: Event): F[Unit] = ev match {
    case CarRegistered(plate, model) => store.registerCar(plate, model).as(()).liftTo[F]

    case CarRepaired(plate) => store.repairCar(plate).as(()).liftTo[F]

    case PartMarked(plate, part) =>  store.markPart(plate, part).as(()).liftTo[F]

    case PartUnmarked(plate, part) => store.unmarkPart(plate, part).as(()).liftTo[F]

    case PartRepaired(plate, part) => store.repairPart(plate, part).as(()).liftTo[F]
  }
}

object CarsStoreEventHandler {
  def apply[G[_] : Monad, F[_] : Effect](store: StoreAlgebra[G], elog: EventLogAlgebra[F])
                                        (implicit trans: G ~> F, ec: ExecutionContext) = new CarsStoreEventHandler(store, elog)
}
