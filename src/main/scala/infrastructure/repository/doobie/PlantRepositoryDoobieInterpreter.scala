package infrastructure.repository
package doobie
import domain._


class PlantRepositoryDoobieInterpreter[F[_]]  {

  def get(id: PlantId): F[Option[Plant]] = ???

  def findByName(name: String): F[Set[Plant]] = ???
}
