import cats.effect.{Effect, IO}
import config.DatabaseConfig

import fs2._
import org.http4s.server.blaze.BlazeBuilder

import scala.concurrent.ExecutionContext.Implicits.global

object Server extends ReadSideServer[IO]

class ReadSideServer[F[_] : Effect] extends StreamApp[F] {


  def stream(args: List[String], requestShutdown: F[Unit]): Stream[F, StreamApp.ExitCode] =
    for {
      ctx <- Stream.eval(Module.init)
      _ <- Stream.eval(DatabaseConfig.initializeDb(ctx.xa))
      exitCode <- BlazeBuilder[F] // Start the server
        .bindHttp(8081)
        .mountService(ctx.endpoints)
        .serve
        .concurrently(ctx.storeEventHandler.process()) //Start the store event handler in the background
    } yield exitCode


}