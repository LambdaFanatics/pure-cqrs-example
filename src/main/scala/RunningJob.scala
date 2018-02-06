import cats.effect.Async
import cats.implicits._
import infrastructure.repository.inmemory.PlantRepositoryInMemoryInterpreter

import domain._

object RunningJob extends App {
  def program0[F[_]: Async](repo: PlantRepository[F]) = for {
    p1  <- repo.get("1")
    p2  <- repo.findByName("plant 1")
  } yield (p1, p2)


  import cats.effect.IO

  // Run with InMemory with IO
  val plants  = program0(new PlantRepositoryInMemoryInterpreter[IO]).unsafeRunSync()
  println(plants)


  // Run with database with IO
}


