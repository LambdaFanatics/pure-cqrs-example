package interpreter.doobie

import cats.Monad
import cats.data.OptionT
import cats.implicits._
import domain.{Plant, PlantId, PlantStoreAlgebra}
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._

//TODO remove after domain changes
class PlantStoreDoobieInterpreter[F[_] : Monad](val xa: Transactor[F]) extends PlantStoreAlgebra[F] {

  private object PlantSQL {


    def insert(plant: Plant): Update0 =
      sql"""INSERT INTO plants (id, name, country)
            VALUES (${plant.id.value}, ${plant.name}, ${plant.country})"""
        .update


    def delete(id: PlantId): Update0 = sql"""DELETE FROM plants WHERE id =${id.value}"""
      .update

    def select(id: PlantId): Query0[Plant] =
      sql"""SELECT id, name, country FROM plants WHERE id = ${id.value}"""
        .query

    def selectByName(name: String): Query0[Plant] =
      sql"""
          SELECT id, name, country
          FROM plants
          WHERE name = $name"""
        .query

    def selectAll(): Query0[Plant] =
      sql"""
          SELECT id, name, country
          FROM plants"""
        .query

  }

  import PlantSQL._

  def create(plant: Plant): F[Plant] = insert(plant).run.transact(xa).as(plant)

  def delete(id: PlantId): F[Option[Plant]] = OptionT(get(id))
    .semiflatMap(plant =>
      PlantSQL.delete(id).run.transact(xa).as(plant)
    ).value

  def get(id: PlantId): F[Option[Plant]] = select(id).option.transact(xa)

  def findByName(name: String): F[Option[Plant]] = selectByName(name).option.transact(xa)

  def list(): F[List[Plant]] = selectAll().to[List].transact(xa)
}

object PlantStoreDoobieInterpreter {
  def apply[F[_] : Monad](xa: Transactor[F]) = new PlantStoreDoobieInterpreter(xa)
}
