package interpreter.doobie

import cats.data.OptionT
import cats.data.OptionT.liftF
import cats.implicits._
import domain.CarStoreAlgebra
import domain.cars._
import doobie._
import doobie.implicits._
import doobie.util.meta.Meta


class CarStoreDoobieInterpreter extends CarStoreAlgebra[ConnectionIO] {

  private object queries {

    implicit val carStatusMeta: Meta[CarStatus] =
      Meta[String].xmap(CarStatus.apply, CarStatus.nameOf)

    def insert(car: Car): Update0 =
      sql"""
      INSERT INTO cars (reg_plate, model, status)
      VALUES  ${car.regPlate}, ${car.model}, ${car.status})
    """.update

    def update(car: Car): Update0 =
      sql"""
      UPDATE cars
      SET status = ${car.status}, model = ${car.model}
      WHERE reg_plate = ${car.regPlate}
    """.update

    def select(regPlate: String): Query0[Car] =
      sql"""SELECT reg_plate, model, status FROM cars WHERE reg_plate = $regPlate"""
        .query

    def selectAll: Query0[Car] =
      sql"""
      SELECT reg_plate, status
      FROM cars
      ORDER BY reg_plate
    """.query
  }


  def create(car: Car): ConnectionIO[Car] =
    queries.insert(car).run.as(car)

  def update(car: Car): ConnectionIO[Option[Car]] = (for {
      _ <- OptionT(get(car.regPlate))
      _ <- liftF(queries.update(car).run)
    } yield car
  ).value


  def modify(regPlate: String)(f: Car => Car): ConnectionIO[Option[Car]] = (for {
      car <- OptionT(get(regPlate))
      changed = f(car)
      _ <- liftF(queries.update(changed).run)
    } yield changed
  ).value


  def get(regPlate: String): ConnectionIO[Option[Car]] =
    queries.select(regPlate).option

  def list(): ConnectionIO[List[Car]] =
    queries.selectAll.to[List]
}

object CarStoreDoobieInterpreter {
  def apply() = new CarStoreDoobieInterpreter()
}

