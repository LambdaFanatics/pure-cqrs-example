import cats.effect.{Effect, IO}
import config.{ApplicationConfig, DatabaseConfig}
import domain.CommandsService
import endpoint.CommandEndpoints
import fs2.StreamApp.ExitCode
import fs2.{Stream, StreamApp, async}
import org.http4s.server.blaze.BlazeBuilder
import interpreter.doobie.EventLogDoobieInterpreter
import interpreter.CommandsInterpreter
import interpreter.memory.ValidationInMemoryInterpreter


object Server extends StreamApp[IO] {

  import scala.concurrent.ExecutionContext.Implicits.global

  def stream(args: List[String], shutdown: IO[Unit]): Stream[IO, ExitCode] =
    createStream[IO](args, shutdown)

  def createStream[F[_] : Effect](args: List[String], shutdown: F[Unit]): Stream[F, ExitCode] =
    for {
      conf <- Stream.eval(ApplicationConfig.load[F]("write-side-server"))
      xa <- Stream.eval(DatabaseConfig.dbTransactor[F](conf.db))
      _ <- Stream.eval(DatabaseConfig.initializeDb(xa))
      eventLog = EventLogDoobieInterpreter(xa)        // This is needed for validation memory synchronization
      validationSemaphore <- Stream.eval(async.semaphore(1))
      validation = ValidationInMemoryInterpreter[F](validationSemaphore)
      commands = CommandsInterpreter[F](eventLog, validation)
      service = CommandsService[F](commands)
      exitCode <- BlazeBuilder[F]
        .bindHttp(8080, "localhost")
        .mountService(CommandEndpoints.endpoints(service))
        .serve
    } yield exitCode
}
