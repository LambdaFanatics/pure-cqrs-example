import cats.Applicative
import cats.effect.Async
import cats.implicits._

import scala.collection.concurrent.TrieMap

trait EventSourcing {

}


//Model


object model {
  case class PlantId(value: String) extends AnyVal
  implicit def stringToPlantId(value: String): PlantId = PlantId(value)
}

import model._



case class Plant(plantId: PlantId, name: String, country: String)


// Algerbras
trait PlantRepository[F[_]] {
  def get(id: PlantId): F[Option[Plant]]

  def findByName(name: String): F[Set[Plant]]
}


// Interpreters

class PlantRepositoryInMemoryInterpreter[F[_]: Applicative] extends PlantRepository[F] {

  private val cache = new TrieMap[PlantId, Plant]
  cache += (PlantId("1") -> Plant("1", "plant 1", "greece"))


  def get(plantId: PlantId): F[Option[Plant]] = cache.get(plantId).pure[F]

  def findByName(name: String): F[Set[Plant]] = ???
}

object RunningJob extends App {
  def program0[F[_]: Async](repo: PlantRepository[F]): F[(Option[Plant], Option[Plant])] = for {
    p1  <- repo.get("1")
    p2  <- repo.get("2")
  } yield (p1, p2)


  import cats.effect.IO

  // Run with InMemory with IO
  val plants  = program0(new PlantRepositoryInMemoryInterpreter[IO]).unsafeRunSync()
  println(plants)


  // Run with database with IO
}


