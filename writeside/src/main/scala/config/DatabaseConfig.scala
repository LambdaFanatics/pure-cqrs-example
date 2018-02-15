package config

import cats.effect.Async
import doobie.hikari.HikariTransactor
import org.flywaydb.core.Flyway

case class DatabaseConfig(url: String, driver: String, user: String, password: String)

object DatabaseConfig {
  def dbTransactor[F[_] : Async] (config: DatabaseConfig): F[HikariTransactor[F]] = HikariTransactor.newHikariTransactor[F](
    config.driver,
    config.url ,
    config.user,
    config.password)

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
