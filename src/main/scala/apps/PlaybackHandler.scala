package apps

import cats.effect.{Effect, IO}
import config.{ApplicationConfig, DatabaseConfig}
import fs2.StreamApp.ExitCode
import fs2.{Scheduler, Stream, StreamApp}
import infrastructure.repository.doobie.EventLogDoobieInterpreter

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object PlaybackHandler extends StreamApp[IO] {
  implicit val ec: ExecutionContext     = scala.concurrent.ExecutionContext.Implicits.global

  def stream(args: List[String], requestShutdown: IO[Unit]): Stream[IO, ExitCode] =
    createStream[IO](args, requestShutdown)


  //FIXME - THIS LIVELOCKS... and honestly I just copied from here https://github.com/gvolpe/fs2-rabbit/blob/master/core/src/main/scala/com/github/gvolpe/fs2rabbit/StreamLoop.scala

  def loop[F[_]: Effect, A](program: Stream[F,A], every: FiniteDuration)(implicit ec: ExecutionContext) : Stream[F, A] = {
    val scheduledProgram = Scheduler[F](4)
      .flatMap(_.sleep[F](every))
      .flatMap(_ => program)

    for {
      _ <- Stream.eval(Effect[F].delay(println("Consuming log...")))
      _ <- program
      p <- loop(scheduledProgram, every)
    } yield p
  }

  def createStream[F[_] : Effect](args: List[String], shutdown: IO[Unit]): Stream[F, ExitCode] =
    for {
      conf <- Stream.eval(ApplicationConfig.load[F])
      xa <- Stream.eval(DatabaseConfig.dbTransactor[F](conf.db))
      consumer = EventLogDoobieInterpreter[F](xa)
      //TODO here map the events to an algebra (store to db, push to a WS [how?] etc...)
      readLogProgram  = consumer.consume().map(ev => {println(ev); ev}).fold(0)((prev, _) => prev + 1)
      _ <- loop(readLogProgram, 5.second)

    } yield ExitCode.Success
}



