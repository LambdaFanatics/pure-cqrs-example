import cats.effect.Effect
import cats.implicits._
import config.{ApplicationConfig, DatabaseConfig}
import domain.{CommandsService, ValidatorReplayHandler}
import doobie.hikari.HikariTransactor
import fs2.async
import interpreter.CommandsInterpreter
import interpreter.doobie.EventLogDoobieInterpreter
import interpreter.memory.ValidationInMemoryInterpreter

import scala.concurrent.ExecutionContext

case class Context[F[_]](config: ApplicationConfig,
                         xa: HikariTransactor[F],
                         commandsService: CommandsService[F],
                         replayService: ValidatorReplayHandler[F])


/**
  * This is manual dependency injeaction with an effectful way
  */
object Context {

  def apply[F[_]: Effect](implicit ec: ExecutionContext): F[Context[F]] = for {
    // Load config
    config <- ApplicationConfig.load("write-side-server")

    // Initialize database transactor
    xa <- DatabaseConfig.dbTransactor(config.db)

    // Initialize interpreters
    eventLog  = EventLogDoobieInterpreter(xa)
    semaphore  <- async.semaphore(1)
    validation  = ValidationInMemoryInterpreter(semaphore)
    commands  = CommandsInterpreter(eventLog, validation)

    // Initialize services
    commandsService  = CommandsService(commands)
    replayService  =  ValidatorReplayHandler(validation, eventLog)

  } yield Context(config, xa, commandsService, replayService)

}

