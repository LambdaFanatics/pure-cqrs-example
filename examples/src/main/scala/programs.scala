package apps

import java.util.UUID

import cats.FlatMap
import cats.implicits._
import domain._

object programs {
  def fetchPlants[F[_]: FlatMap](repo: PlantStoreAlgebra[F]) =
    for {
      p1 <- repo.get(UUID.randomUUID())
      p2 <- repo.findByName("plant 1")
      all <- repo.list()
    } yield (p1, p2, all)


  def createAndFetchPlants[F[_]: FlatMap](repo: PlantStoreAlgebra[F]) =
    for {
      _ <- repo.create(Plant(UUID.randomUUID(), "NEMESIS", "GREECE"))
      _ <- repo.create(Plant(UUID.randomUUID(), "TISIS", "GREECE"))
      _ <- repo.create(Plant(UUID.randomUUID(), "TALOS", "GREECE"))
      all <- repo.list()
    } yield all


  def createDeleteAndFetch[F[_]: FlatMap](repo: PlantStoreAlgebra[F]) =
    for {
      p <- repo.create(Plant(UUID.randomUUID(), "NEMESIS", "GREECE"))
      _ <- repo.delete(p.id)
      all <- repo.list()
    } yield all
}
