package domain

trait PlantCommandsAlgebra[F[_]] {
  def create(name: String, country: String): F[Unit]
  def delete(id: PlantId): F[Unit]
}
