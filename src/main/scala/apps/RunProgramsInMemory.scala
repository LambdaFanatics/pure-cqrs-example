package apps

import cats.effect.IO
import infrastructure.repository.inmemory.PlantRepoInMemoryInterpreter


// These should be converted to tests
object RunProgramsInMemory extends App {

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


