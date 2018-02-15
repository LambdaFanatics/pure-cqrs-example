package domain

trait PlantStoreAlgebra[F[_]] {

  def create(plant: Plant): F[Plant]

  def delete(id: PlantId): F[Option[Plant]]

  def get(id: PlantId): F[Option[Plant]]

  def findByName(name: String): F[Option[Plant]]

  def list(): F[List[Plant]]

}
