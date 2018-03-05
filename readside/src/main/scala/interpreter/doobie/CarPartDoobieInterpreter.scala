package interpreter.doobie

import domain.CarPartStoreAlgebra
import domain.cars._
import doobie._
import doobie.implicits._
import cats.implicits._

class CarPartDoobieInterpreter extends CarPartStoreAlgebra[ConnectionIO] {

  private object queries {
    import instances._

    def insert(part: CarPart): Update0 = sql"""
      INSERT INTO car_parts (name, car_reg_plate, status)
      VALUES  , ${part.name}, ${part.carPlate}, ${part.status})
    """.update


    def update(part: CarPart) : Update0 = sql"""
      UPDATE car_parts
      SET status = ${part.status}
      WHERE car_reg_plate = ${part.carPlate} AND status = ${part.status}
    """.update

    def selectByNameAndCarPlate(name: String, carPlate: String ): Query0[CarPart] =
      sql"""SELECT name, car_reg_plate, status
            FROM car_parts
            WHERE name = $name AND reg_plate = $carPlate"""
        .query

    def selectAll : Query0[CarPart] = sql"""
      SELECT name, car_reg_plate, status
      FROM cars
      ORDER BY car_reg_plate
    """.query


  }

  def create(part: CarPart): ConnectionIO[CarPart] =
    queries.insert(part).run.as(part)


  def update(part: CarPart): ConnectionIO[CarPart] =
    queries.update(part).run.as(part)


  def findByNameAndPlate(name: String, carPlate: String): ConnectionIO[Option[CarPart]] =
    queries.selectByNameAndCarPlate(name, carPlate).option

  def list(): ConnectionIO[List[CarPart]] =
    queries.selectAll.to[List]
}



