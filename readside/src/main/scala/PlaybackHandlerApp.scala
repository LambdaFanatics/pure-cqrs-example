import cats.effect.{Effect, IO}
import config.{ApplicationConfig, DatabaseConfig}
import domain.SeekBeginning
import fs2.StreamApp.ExitCode
import fs2.{Stream, StreamApp}
import interpreter.doobie.EventLogDoobieInterpreter
import utils.stream._

import scala.concurrent.ExecutionContext

object PlaybackHandlerApp extends StreamApp[IO] {
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  def stream(args: List[String], requestShutdown: IO[Unit]): Stream[IO, ExitCode] =
      createStream[IO](args, requestShutdown).drain

  def createStream[F[_] : Effect](args: List[String], shutdown: IO[Unit]): Stream[F,ExitCode] = {
    for {
      conf <- Stream.eval(ApplicationConfig.load[F]("read-side-server"))
      xa <- Stream.eval(DatabaseConfig.dbTransactor[F](conf.db))
      eventLog = EventLogDoobieInterpreter[F](xa)
      _ <- eventLog.consume("playback-handler", SeekBeginning, closeOnEnd = true).through(logStrLn("consumer"))
    } yield ExitCode.Success
  }
}



