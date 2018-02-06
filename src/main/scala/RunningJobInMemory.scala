import infrastructure.repository.inmemory.PlantRepoInMemoryInterpreter
import cats.effect.IO


// Run with InMemory with IO
object RunningJobInMemory extends App {

  import programs._

  var repo = PlantRepoInMemoryInterpreter[IO]

  val res1 = fetchPlants(repo).unsafeRunSync()
  println(res1)

  repo = PlantRepoInMemoryInterpreter[IO]

  val res2 = createAndFetchPlants(repo).unsafeRunSync()
  println(res2)

  repo = PlantRepoInMemoryInterpreter[IO]

  val res3 = createDeleteAndFetch(repo).unsafeRunSync()
  println(res3)
}


