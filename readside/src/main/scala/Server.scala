import cats.effect.{Effect, IO}
import fs2.StreamApp
import org.http4s.server.blaze.BlazeBuilder

import scala.concurrent.ExecutionContext.Implicits.global
object Server extends ReadSideServer[IO]



class ReadSideServer[F[_] : Effect] extends StreamApp[F] {
  def stream(args: List[String], requestShutdown: F[Unit]): fs2.Stream[F, StreamApp.ExitCode] =
    for {
      exitCode <- BlazeBuilder[F]
        .bindHttp(8081)
        .serve
    } yield exitCode
}