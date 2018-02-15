package apps

import cats.effect.{Effect, IO}
import fs2.StreamApp.ExitCode
import fs2.{Pipe, Scheduler, Stream, StreamApp}
import infrastructure.repository.doobie.EventLogDoobieInterpreter

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object PlaybackHandler extends StreamApp[IO] {
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  def stream(args: List[String], requestShutdown: IO[Unit]): Stream[IO, ExitCode] =
    createStream[IO](args, requestShutdown).drain.as(ExitCode.Success) // TODO: Search what happens with drain? why without it we don't consume the stream I miss something here...


  def loopWithDelay[F[_]: Effect, A](program: Stream[F,A], every: FiniteDuration)(implicit ec: ExecutionContext) : Stream[F, A] = {
       Scheduler[F](2).flatMap(f => (program ++ f.sleep_[F](every)).repeat )
  }

  def log[F[_]: Effect,A](prefix: String): Pipe[F, A, A] = _.evalMap(a => Effect[F].delay { println(s"[$prefix - ${Thread.currentThread.getName}] $a "); a})

  def createStream[F[_] : Effect](args: List[String], shutdown: IO[Unit]): Stream[F,Unit] = {
    for {
      conf <- Stream.eval(ApplicationConfig.load[F])
      xa <- Stream.eval(DatabaseConfig.dbTransactor[F](conf.db))
      eventLog = EventLogDoobieInterpreter[F](xa)
      //TODO here map the events to an algebra (store to db, push to a WS [how?] etc...)
      readLogProgram =  eventLog.consume().through(log("consumer"))
      _ <- loopWithDelay(readLogProgram, 10.second)
    } yield ()
  }
}



