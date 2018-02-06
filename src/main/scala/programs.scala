import cats.FlatMap
import cats.implicits._
import domain._

object programs {
  def fetchPlants[F[_]: FlatMap](repo: PlantRepoAlgebra[F]) =
    for {
      p1 <- repo.get("1")
      p2 <- repo.findByName("plant 1")
      all <- repo.list()
    } yield (p1, p2, all)


  def createAndFetchPlants[F[_]: FlatMap](repo: PlantRepoAlgebra[F]) =
    for {
      _ <- repo.create(Plant("", "NEMESIS", "GREECE"))
      _ <- repo.create(Plant("", "TISIS", "GREECE"))
      _ <- repo.create(Plant("", "TALOS", "GREECE"))
      all <- repo.list()
    } yield all


  def createDeleteAndFetch[F[_]: FlatMap](repo: PlantRepoAlgebra[F]) =
    for {
      p <- repo.create(Plant("", "NEMESIS", "GREECE"))
      _ <- repo.delete(p.id)
      all <- repo.list()
    } yield all
}
