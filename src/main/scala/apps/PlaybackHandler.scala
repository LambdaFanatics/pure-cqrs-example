package apps

import cats.effect.{Effect, IO}
import config.{ApplicationConfig, DatabaseConfig}
import fs2.StreamApp.ExitCode
import fs2.{Stream, StreamApp}
import infrastructure.repository.doobie.EventLogDoobieInterpreter

object PlaybackHandler extends StreamApp[IO] {
  def stream(args: List[String], requestShutdown: IO[Unit]): Stream[IO, ExitCode] =
    createStream[IO](args, requestShutdown)


  def createStream[F[_]](args: List[String], shutdown: IO[Unit])
                        (implicit F: Effect[F]): Stream[F, ExitCode] = for {
    conf <- Stream.eval(ApplicationConfig.load[F])
    xa <- Stream.eval(DatabaseConfig.dbTransactor[F](conf.db))
    consumer = EventLogDoobieInterpreter[F](xa)
    _ <- consumer.consume().map(ev => {
      println(ev); ev
    })

  } yield ExitCode.Success
}



