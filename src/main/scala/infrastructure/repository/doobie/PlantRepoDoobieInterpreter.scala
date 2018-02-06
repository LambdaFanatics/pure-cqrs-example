package infrastructure.repository.doobie

import cats.Monad
import cats.data.OptionT
import cats.implicits._
import domain._
import doobie._
import doobie.implicits._


class PlantRepoDoobieInterpreter[F[_] : Monad](val xa: Transactor[F]) extends PlantRepoAlgebra[F] {

  private object PlantSQL {


    def insert(plant: Plant): Update0 =
      sql"""
        INSERT INTO plants (name, country)
        VALUES (${plant.name}, ${plant.country})
      """.update


    def delete(id: PlantId): Update0 = sql"""
      DELETE FROM plants WHERE id = $id
    """.update

    def select(id: PlantId): Query0[Plant] =
      sql"""
          SELECT id, name, country
          FROM plants
          WHERE id = ${id.value}"""
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

  def create(plant: Plant): F[Plant] = insert(plant)
    .withUniqueGeneratedKeys[String]("id")
    .map(id => plant.copy(id = id))
    .transact(xa)



  def delete(id: PlantId): F[Option[Plant]] = OptionT(get(id))
    .semiflatMap( plant =>
      PlantSQL.delete(id).run.transact(xa).as(plant)
    ).value

  def get(id: PlantId): F[Option[Plant]] = select(id).option.transact(xa)

  def findByName(name: String): F[Option[Plant]] = selectByName(name).option.transact(xa)

  def list(): F[List[Plant]] = selectAll().list.transact(xa)
}

object PlantRepoDoobieInterpreter {
  def apply[F[_] : Monad](xa: Transactor[F]) = new PlantRepoDoobieInterpreter(xa)
}
