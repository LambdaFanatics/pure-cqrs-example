import infrastructure.repository.inmemory.PlantRepositoryInMemoryInterpreter
import cats.effect.IO


// Run with InMemory with IO
object RunningJobInMemory extends App {
  val plants  = programs.fetchPlants(new PlantRepositoryInMemoryInterpreter[IO]).unsafeRunSync()
  println(plants)
}


