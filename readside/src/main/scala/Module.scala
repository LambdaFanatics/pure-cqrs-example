import cats._
import cats.effect.Effect
import cats.implicits._
import config.{ApplicationConfig, DatabaseConfig}
import domain._
import doobie.ConnectionIO
import doobie.hikari.HikariTransactor
import doobie.implicits._
import endpoint.CarEndpoints
import interpreter.StoreInterpreter
import interpreter.doobie.{CarPartStoreDoobieInterpreter, CarStoreDoobieInterpreter, EventLogDoobieInterpreter}
import org.http4s.HttpService
import service.CarService
import utils.functional.connectionIOToMonad


/**
  * This is manual dependency injection.
  */
class Module[F[_] : Effect](config: ApplicationConfig, val xa: HikariTransactor[F]) {
  private lazy val carStore =
    new CarStoreDoobieInterpreter()

  private lazy val partStore =
    new CarPartStoreDoobieInterpreter()

  private lazy val store =
    new StoreInterpreter(carStore, partStore)

  private lazy val eventLog =
    new EventLogDoobieInterpreter(xa)


  private implicit val trans: ConnectionIO ~> F =
    connectionIOToMonad(xa)

  private lazy val carService: CarService[ConnectionIO, F] =
    new CarService(store)

  private lazy val carEndpoints =
    new CarEndpoints(carService)

  lazy val storeEventHandler: CarsStoreEventHandler[ConnectionIO, F] =
    new CarsStoreEventHandler(store, eventLog)

  lazy val endpoints: HttpService[F] =
    carEndpoints.endpoints()
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

