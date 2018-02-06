package domain

trait PlantRepository[F[_]] {
  def get(id: PlantId): F[Option[Plant]]

  def findByName(name: String): F[Set[Plant]]
}
