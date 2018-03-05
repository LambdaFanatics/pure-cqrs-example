package interpreter.doobie

import domain.CarStoreAlgebra
import domain.cars._
import doobie._
import doobie.implicits._
import cats.implicits._



class CarStoreDoobieInterpreter extends CarStoreAlgebra[ConnectionIO]{

  private object queries {
    import instances._

    def insert(car: Car): Update0 = sql"""
      INSERT INTO cars (reg_plate, model, status)
      VALUES  ${car.regPlate}, ${car.model}, ${car.status})
    """.update

    def update(car: Car) : Update0 = sql"""
      UPDATE cars
      SET status = ${car.status}, model = ${car.model}
      WHERE reg_plate = ${car.regPlate}
    """.update

    def select(regPlate: String): Query0[Car] =
      sql"""SELECT reg_plate, model, status FROM cars WHERE reg_plate = $regPlate"""
        .query

    def selectAll : Query0[Car] = sql"""
      SELECT reg_plate, status
      FROM cars
      ORDER BY reg_plate
    """.query
  }


  def create(car: Car): ConnectionIO[Car] =
    queries.insert(car).run.as(car)

  def get(regPlate: String): ConnectionIO[Option[Car]] =
    queries.select(regPlate).option

  def update(car: Car): ConnectionIO[Car] =
    queries.update(car).run.as(car)

  def list(): ConnectionIO[List[Car]] =
    queries.selectAll.to[List]
}

object CarStoreDoobieInterpreter {
  def apply() = new CarStoreDoobieInterpreter()
}

