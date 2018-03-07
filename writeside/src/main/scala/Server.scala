import cats.effect.{Effect, IO}
import config.{ApplicationConfig, DatabaseConfig}
import domain.{CommandsService, ValidationReplayHandler}
import endpoint.CommandEndpoints
import fs2.StreamApp.ExitCode
import fs2.{Stream, StreamApp, async}
import interpreter.CommandsInterpreter
import interpreter.doobie.EventLogDoobieInterpreter
import interpreter.memory.ValidationInMemoryInterpreter
import org.http4s.server.blaze.BlazeBuilder
import scala.concurrent.ExecutionContext.Implicits.global


object Server extends WriteSideServer[IO]

class WriteSideServer[F[_]: Effect]  extends StreamApp[F]{

  def stream(args: List[String], shutdown: F[Unit]): Stream[F, ExitCode] =
    createStream(args, shutdown)

  def createStream(args: List[String], shutdown: F[Unit]): Stream[F, ExitCode] =
    // TODO: We need a module in order to define all this dependency injection...
    for {
      conf <- Stream.eval(ApplicationConfig.load[F]("write-side-server"))
      xa <- Stream.eval(DatabaseConfig.dbTransactor[F](conf.db))
      _ <- Stream.eval(DatabaseConfig.initializeDb(xa))
      eventLog = EventLogDoobieInterpreter(xa)
      validationSemaphore <- Stream.eval(async.semaphore(1)) // This is needed for validation memory synchronization
      validation = ValidationInMemoryInterpreter[F](validationSemaphore)
      commands = CommandsInterpreter[F](eventLog, validation)
      commandsService = CommandsService[F](commands)
      replayService = ValidationReplayHandler(validation, eventLog)

      exitCode <-
        replayService.initializeState.as(ExitCode.Success) ++   // First run the replay service
          BlazeBuilder[F]
            .bindHttp(8080, "localhost")
            .mountService(CommandEndpoints.endpoints(commandsService))
            .serve
    } yield exitCode
}
