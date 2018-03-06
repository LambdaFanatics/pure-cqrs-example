package interpreter.doobie

import cats.data.OptionT
import cats.data.OptionT.liftF
import cats.implicits._
import domain.CarPartStoreAlgebra
import domain.parts._
import doobie._
import doobie.implicits._
import doobie.util.meta.Meta

class CarPartStoreDoobieInterpreter extends CarPartStoreAlgebra[ConnectionIO] {

  private object queries {

    implicit val cparStatusMeta: Meta[PartStatus] =
      Meta[String].xmap(PartStatus.apply, PartStatus.nameOf)


    def insert(part: CarPart): Update0 =
      sql"""INSERT INTO car_parts
            (name, car_reg_plate, status)
            VALUES  , ${part.name}, ${part.carPlate}, ${part.status})
      """.update


    def update(part: CarPart): Update0 =
      sql"""UPDATE car_parts
            SET status = ${part.status}
            WHERE car_reg_plate = ${part.carPlate} AND status = ${part.status}
      """.update

    def selectByNameAndPlate(name: String, carPlate: String): Query0[CarPart] =
      sql"""SELECT name, car_reg_plate, status
            FROM car_parts
            WHERE name = $name AND car_reg_plate = $carPlate
        """.query

    def deleteByNameAndPlate(name: String, carPlate: String): Update0 =
      sql"""DELETE FROM car_parts
            WHERE car_reg_plate = $carPlate AND name= $name
        """.update

    def selectAll: Query0[CarPart] =
      sql"""SELECT name, car_reg_plate, status
            FROM cars
            ORDER BY car_reg_plate
      """.query
  }

  def create(part: CarPart): ConnectionIO[CarPart] =
    queries.insert(part).run.as(part)


  def update(part: CarPart): ConnectionIO[Option[CarPart]] = {
    val res = for {
      _ <- OptionT(get(part.name, part.carPlate))
      - <- liftF(queries.update(part).run)
    } yield part
    res.value
  }

  def modify(regPlate: String, name: String)(f: CarPart => CarPart): ConnectionIO[Option[CarPart]] = {
    val res = for {
      part  <- OptionT(get(name, regPlate))
      changed = f(part)
      _     <- liftF(queries.update(changed).run)
    } yield changed
    res.value
  }

  def delete(name: String, carPlate: String): ConnectionIO[Option[CarPart]] = (
      for {
        part <- OptionT(get(name, carPlate))
        _ <- liftF(queries.deleteByNameAndPlate(name, carPlate).run)
      } yield part
    ).value


  def get(name: String, carPlate: String): ConnectionIO[Option[CarPart]] =
    queries.selectByNameAndPlate(name, carPlate).option

  def list(): ConnectionIO[List[CarPart]] =
    queries.selectAll.to[List]
}



