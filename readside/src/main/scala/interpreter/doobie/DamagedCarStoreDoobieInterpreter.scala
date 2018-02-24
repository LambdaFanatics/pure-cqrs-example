package interpreter.doobie

import cats.effect.Effect
import domain.DamagedCarStoreAlgebra
import domain.cars._
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import cats.implicits._


class DamagedCarStoreDoobieInterpreter[F[_]: Effect](val xa: Transactor[F]) extends DamagedCarStoreAlgebra[F]{

  private object queries {

    implicit val statusMeta: Meta[CarStatus] =
      Meta[String].xmap(CarStatus.apply, CarStatus.nameOf)

    def insert(car: DamagedCar): Update0 = sql"""
      INSERT INTO damaged_cars (id, registration_plate, status)
      VALUES (${car.id}, ${car.regPlate}, ${car.status})
    """.update


    def update(car: DamagedCar) : Update0 = sql"""
      UPDATE damaged_cars
      SET registration_plate = ${car.regPlate}, status = ${car.status}
      WHERE id = ${car.id}
    """.update

      def selectAll : Query0[DamagedCar] = sql"""
      SELECT id, registration_plate, status
      FROM damaged_cars
      ORDER BY registration_plate
    """.query
  }


  def create(car: DamagedCar): F[DamagedCar] =
    queries.insert(car).run.transact(xa).as(car)

  def update(car: DamagedCar): F[DamagedCar] =
    queries.update(car).run.transact(xa).as(car)

  def list(): F[List[DamagedCar]] =
    queries.selectAll.to[List].transact(xa)
}

object DamagedCarStoreDoobieInterpreter {
  def apply[F[_]: Effect](xa: Transactor[F]) = new DamagedCarStoreDoobieInterpreter(xa)
}

