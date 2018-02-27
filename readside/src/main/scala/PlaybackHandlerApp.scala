import StreamUtils._
import cats.effect.{Effect, IO}
import config.{ApplicationConfig, DatabaseConfig}
import fs2.StreamApp.ExitCode
import fs2.{Stream, StreamApp}
import interpreter.doobie.EventLogDoobieInterpreter

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object PlaybackHandlerApp extends StreamApp[IO] {
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  def stream(args: List[String], requestShutdown: IO[Unit]): Stream[IO, ExitCode] =
      createStream[IO](args, requestShutdown).drain

  def createStream[F[_] : Effect](args: List[String], shutdown: IO[Unit]): Stream[F,ExitCode] = {
    for {
      conf <- Stream.eval(ApplicationConfig.load[F]("read-side-server"))
      xa <- Stream.eval(DatabaseConfig.dbTransactor[F](conf.db))
      eventLog = EventLogDoobieInterpreter[F](xa)
      readLogProgram =  eventLog.consume().through(logStrLn("consumer"))
      _ <- loopWithInterval(readLogProgram, 1.second)
    } yield ExitCode.Success
  }
}



