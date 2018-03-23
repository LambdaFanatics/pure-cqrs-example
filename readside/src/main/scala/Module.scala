import config.{ApplicationConfig, DatabaseConfig}
import domain._
import doobie.hikari.HikariTransactor
import interpreter.StoreInterpreter
import interpreter.doobie.{CarPartStoreDoobieInterpreter, CarStoreDoobieInterpreter, EventLogDoobieInterpreter}
import utils.functional.connectionIOToMonad
import doobie.ConnectionIO
import doobie.implicits._
import cats._
import cats.effect.Effect
import cats.implicits._


/**
  * This is manual dependency injection.
  */
class Module[F[_] : Effect](config: ApplicationConfig, val xa: HikariTransactor[F]) {
  private lazy val carStore =
    new CarStoreDoobieInterpreter()

  private lazy val partStore =
    new CarPartStoreDoobieInterpreter()

  private lazy val store: StoreInterpreter[ConnectionIO] =
    new StoreInterpreter(carStore, partStore)

  private lazy val eventLog =
    new EventLogDoobieInterpreter(xa)

  private implicit val trans: ~>[ConnectionIO, F] = connectionIOToMonad(xa)

  lazy val storeEventHandler =
    new CarsStoreEventHandler(store, eventLog)
}

object Module {

  /**
    * This is an effectful way to initialize our module *
    */
  def init[F[_] : Effect]: F[Module[F]] = for {
    // Load config
    config <- ApplicationConfig.load[F]("read-side-server")

    // Initialize database transactor
    xa <- DatabaseConfig.dbTransactor[F](config.db)

  } yield new Module(config, xa)
}

