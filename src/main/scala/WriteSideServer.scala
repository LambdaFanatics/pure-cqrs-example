import cats.effect.{Effect, IO}
import config.ApplicationConfig
import domain.CommandsService
import fs2.StreamApp.ExitCode
import fs2.{Stream, StreamApp}
import infrastructure.endpoint.CommandEndpoints
import org.http4s.server.blaze.BlazeBuilder

object WriteSideServer extends StreamApp[IO] {

  import scala.concurrent.ExecutionContext.Implicits.global

  def stream(args: List[String], shutdown: IO[Unit]): Stream[IO, ExitCode] =
    createStream[IO](args, shutdown)

  def createStream[F[_] : Effect](args: List[String], shutdown: F[Unit]): Stream[F, ExitCode] =
    for {
      conf <- Stream.eval(ApplicationConfig.load[F])
      service = CommandsService[F]
      exitCode <- BlazeBuilder[F]
        .bindHttp(8080, "localhost")
        .mountService(CommandEndpoints.endpoints(service))
        .serve

    } yield exitCode

}
