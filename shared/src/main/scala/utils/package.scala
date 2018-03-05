import cats.effect.{Effect, Sync}
import cats.~>
import fs2.{Pipe, Scheduler, Stream}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration

package object utils {

  object stream {
    /** Extending the stream api
      * See https://github.com/gvolpe/advanced-http4s/blob/master/src/main/scala/com/github/gvolpe/http4s/StreamUtils.scala
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


  object functional {

    // Awesomeness from this great article
    // see https://www.beyondthelines.net/programming/introduction-to-tagless-final/
    implicit class naturalTransformation[F[_],A](fa: F[A]) {
      def liftTo[G[_]](implicit trans: F ~> G): G[A] = trans(fa)
    }
  }
}
