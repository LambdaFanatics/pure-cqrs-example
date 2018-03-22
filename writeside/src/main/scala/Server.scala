import cats.effect.{Effect, IO}
import config.DatabaseConfig
import endpoint.CommandEndpoints
import fs2.StreamApp.ExitCode
import fs2.{Stream, StreamApp}
import org.http4s.server.blaze.BlazeBuilder

import scala.concurrent.ExecutionContext.Implicits.global


object Server extends WriteSideServer[IO]

class WriteSideServer[F[_] : Effect] extends StreamApp[F] {

  def stream(args: List[String], shutdown: F[Unit]): Stream[F, ExitCode] =
    createStream(args, shutdown)

  def createStream(args: List[String], shutdown: F[Unit]): Stream[F, ExitCode] =
    for {
      ctx <- Stream.eval(Module.init)
      _ <- Stream.eval(DatabaseConfig.initializeDb(ctx.xa))
      exitCode <-
        // First run the replay service & then start the server
        ctx.replayHandler.initializeState().as(ExitCode.Success) ++
          BlazeBuilder[F]
            .bindHttp(8080, "localhost")
            .mountService(CommandEndpoints.endpoints(ctx.commandsService))
            .serve
    } yield exitCode
}
