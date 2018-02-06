package infrastructure.repository.doobie
import cats.Monad
import doobie._
import doobie.implicits._
import domain._

private object PlantSQL {

  def select(id: PlantId): Query0[Plant] =
    sql"""SELECT id, name, country
          FROM plants
          WHERE id = ${id.value}"""
      .query

  def selectByName(name: String): Query0[Plant] =
    sql""" SELECT id, name, country
           FROM plants
           WHERE name = $name"""
      .query

}

class PlantRepositoryDoobieInterpreter[F[_]: Monad](val xa: Transactor[F]) extends PlantRepository[F] {

  import PlantSQL._

  def get(id: PlantId): F[Option[Plant]] = select(id).option.transact(xa)

  def findByName(name: String): F[Option[Plant]] = selectByName(name).option.transact(xa)
}

object PlantRepositoryDoobieInterpreter {
  def apply[F[_]: Monad](xa: Transactor[F]) =
    new PlantRepositoryDoobieInterpreter(xa)
}
