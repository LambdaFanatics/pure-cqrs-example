import cats.effect.IO
import config.{ApplicationConfig, DatabaseConfig}
import interpreter.doobie.PlantStoreDoobieInterpreter
import programs._

// TODO these should be converted to tests
object  RunProgramsDatabase extends App {


  val run = for {
    conf <- ApplicationConfig.load[IO]("read-side-server")
    xa <- DatabaseConfig.dbTransactor[IO](conf.db)
    _ <- DatabaseConfig.initializeDb(xa)  // This recreates and initializes the database
    repo = PlantStoreDoobieInterpreter(xa)
    res1 <- fetchPlants(repo)
    res2 <- createDeleteAndFetch(repo)
    res3 <- createAndFetchPlants(repo)

  } yield (res1, res2, res3).productIterator.toList.mkString("\n")

  val res = run.unsafeRunSync()
  println(res)

}
