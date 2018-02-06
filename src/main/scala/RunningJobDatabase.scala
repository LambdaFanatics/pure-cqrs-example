import cats.effect.IO
import config.DatabaseConfig
import infrastructure.repository.doobie.PlantRepoDoobieInterpreter



object  RunningJobDatabase extends App {
  import programs._

  val run = for {
    xa <- DatabaseConfig.dbTransactor[IO]
    _ <- DatabaseConfig.initializeDb(xa)  // This recreates and initializes the database
    repo = PlantRepoDoobieInterpreter(xa)
    res1 <- fetchPlants(repo)
    res2 <- createDeleteAndFetch(repo)
    res3 <- createAndFetchPlants(repo)

  } yield (res1, res2, res3).productIterator.toList.mkString("\n")

  val res = run.unsafeRunSync()
  println(res)

}
