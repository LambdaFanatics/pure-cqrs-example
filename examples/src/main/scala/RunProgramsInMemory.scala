
import cats.effect.IO
import interpreter.memory.PlantStoreInMemoryInterpreter


// These should be converted to tests
object RunProgramsInMemory extends App {

  import programs._

  var repo = PlantStoreInMemoryInterpreter[IO]()

  val res1 = fetchPlants(repo).unsafeRunSync()
  println(res1)

  repo = PlantStoreInMemoryInterpreter[IO]()

  val res2 = createAndFetchPlants(repo).unsafeRunSync()
  println(res2)

  repo = PlantStoreInMemoryInterpreter[IO]()

  val res3 = createDeleteAndFetch(repo).unsafeRunSync()
  println(res3)
}


