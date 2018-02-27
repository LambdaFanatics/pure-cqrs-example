import cats.effect.{Effect, Sync}
import fs2.{Pipe, Scheduler, Stream}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration

object StreamUtils {
  /** Extending the stream api
    * As seen in advanced http4s https://github.com/gvolpe/advanced-http4s/blob/master/src/main/scala/com/github/gvolpe/http4s/StreamUtils.scala
    */
  trait StreamUtilsOps[F[_]] {
    def evalF[A](thunk: => A)(implicit F: Sync[F]): Stream[F, A] = Stream.eval(F.delay(thunk))
    def putStrLn(value: String)(implicit F: Sync[F]): Stream[F, Unit] = evalF(println(value))
  }

  implicit def syncInstance[F[_]: Sync]: StreamUtilsOps[F] = new StreamUtilsOps[F] {}

  def logStrLn[F[_]: Effect,A](prefix: String): Pipe[F, A, A] =
    _.evalMap(a => Effect[F].delay { println(s"[$prefix - ${Thread.currentThread.getName}] $a "); a})


  def loopWithInterval[F[_]: Effect, A](program: Stream[F,A], interval: FiniteDuration)(implicit ec: ExecutionContext) : Stream[F, A] =
    Scheduler[F](2).flatMap(f => (program ++ f.sleep_[F](interval)).repeat)
}

