import cats.effect.Effect
import cats.implicits._
import config.{ApplicationConfig, DatabaseConfig}
import domain._
import doobie.hikari.HikariTransactor
import fs2.async
import fs2.async.mutable.Semaphore
import interpreter.CommandsInterpreter
import interpreter.doobie.EventLogDoobieInterpreter
import interpreter.memory.ValidationInMemoryInterpreter

import scala.concurrent.ExecutionContext

/**
  * This is manual dependency injection.
  */
class Module[F[_]: Effect](config: ApplicationConfig, val  xa: HikariTransactor[F], sem: Semaphore[F]){

  private lazy val eventLog: EventLogAlgebra[F] =
    new EventLogDoobieInterpreter[F](xa)

  private lazy val validations: ValidationAlgebra[F] =
    new ValidationInMemoryInterpreter[F](sem)

  private lazy val commands: CommandsAlgebra[F]=
    new CommandsInterpreter[F](eventLog, validations)

  lazy val commandsService: CommandsService[F] =
    new CommandsService[F](commands)

  lazy val replayHandler: ValidatorReplayHandler[F] =
    new ValidatorReplayHandler[F](eventLog, validations)
}

object Module {

  /**
    * This is an effectful way to initialize our module *
    */
  def init[F[_]: Effect](implicit ec: ExecutionContext): F[Module[F]] = for {
    // Load config
    config <- ApplicationConfig.load("write-side-server")

    // Initialize database transactor
    xa <- DatabaseConfig.dbTransactor(config.db)

    sem  <- async.semaphore(1)
  } yield new Module(config, xa,  sem)
}

