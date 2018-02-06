import cats.FlatMap
import cats.implicits._
import domain._

object programs {
  def fetchPlants[F[_]: FlatMap](repo: PlantRepository[F]) =
    for {
      p1 <- repo.get("1")
      p2 <- repo.findByName("plant 1")
    } yield (p1, p2)
}
