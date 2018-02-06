import cats.effect.IO
import config.DatabaseConfig



object  RunningJobDatabase extends App {
  val run = for {
    xa <- DatabaseConfig.dbTransactor[IO]
    _ <- DatabaseConfig.initializeDb(xa)
//    repo = new DA
//
//    res <- programs.fetchPlants[IO]()

  } yield xa

  val res = run.unsafeRunSync()
  println(res)

}
