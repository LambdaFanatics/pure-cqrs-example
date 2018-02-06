package config

import cats.effect.Async
import doobie.hikari.HikariTransactor
import org.flywaydb.core.Flyway

case class DatabaseConfig(url: String, driver: String, user: String, password: String)

// TODO create application conf
//petstore {
//db {
//url="jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"
//user="sa"
//password=""
//driver="org.h2.Driver"
//}
//}

object DatabaseConfig {
  def dbTransactor[F[_] : Async]: F[HikariTransactor[F]] = HikariTransactor.newHikariTransactor[F](
    "org.h2.Driver",
    "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
    "sa",
    "")

  def initializeDb[F[_]: Async](xa: HikariTransactor[F]): F[Unit] = {
    xa.configure { ds =>
      Async[F].delay {
        val fw = new Flyway()
        fw.setDataSource(ds)
        fw.migrate()
        ()
      }
    }
  }


}
