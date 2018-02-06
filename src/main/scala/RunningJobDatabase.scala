import cats.effect.IO
import config.DatabaseConfig
import infrastructure.repository.doobie.PlantRepositoryDoobieInterpreter



object  RunningJobDatabase extends App {
  val run = for {
    xa <- DatabaseConfig.dbTransactor[IO]
    _ <- DatabaseConfig.initializeDb(xa)
    repo = PlantRepositoryDoobieInterpreter(xa)
    res <- programs.fetchPlants(repo)

  } yield res

  val res = run.unsafeRunSync()
  println(res)

}
