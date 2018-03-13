import cats.effect.{Effect, Sync}
import cats.{Monad, ~>}
import cats.implicits._

import doobie.free.connection.ConnectionIO
import doobie.util.transactor.Transactor
import fs2.{Pipe, Scheduler, Stream}
import fs2.async._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration


package object utils {

  object stream {
    /**
      * Extending the effectful actions api
      * See https://github.com/gvolpe/advanced-http4s/blob/master/src/main/scala/com/github/gvolpe/http4s/StreamUtils.scala
      */
    trait StreamUtilsOps[F[_]] {
      def evalF[A](thunk: => A)(implicit F: Sync[F]): Stream[F, A] = Stream.eval(F.delay(thunk))
      def putStrLn(value: String)(implicit F: Sync[F]): Stream[F, Unit] = evalF(println(value))
    }

    implicit def syncInstance[F[_]: Sync]: StreamUtilsOps[F] = new StreamUtilsOps[F] {}

    def logStrLn[F[_]: Effect,A](prefix: String): Pipe[F, A, A] =
      _.evalMap(a => Effect[F].delay { println(s"[$prefix - ${Thread.currentThread.getName}] $a "); a})

    /**
      * Run the supplied effectful action using the last element after the end of this stream, regardless of how the stream terminates.
      * This is similar to [[fs2.Stream.InvariantOps.onFinalize]] but preserves
      * the last element processed by the stream.
      *
      * @param f an effectfull action that uses the last element if any.
      * @return a stream
      */
    def afterLastElement[F[_],A](stream: Stream[F,A], f: Option[A] => F[Unit])(implicit F: Effect[F], ec: ExecutionContext): Stream[F, A] =
      Stream.bracket(signalOf[F, Option[A]](None))(
        signal => stream.evalMap( a => signal.set(a.some).as(a)),
        signal => signal.get.flatMap(f(_)))


    def repeatWithInterval[F[_]: Effect, A](stream: Stream[F,A], interval: FiniteDuration)(implicit ec: ExecutionContext) : Stream[F, A] =
      Scheduler[F](2).flatMap(f => (stream ++ f.sleep_[F](interval)).repeat)


    trait StreamExtOps[F[_], A] {
      def repeatWithInterval(interval: FiniteDuration)(implicit ec: ExecutionContext) : Stream[F, A]
      def afterLastElement(f: Option[A] => F[Unit])(implicit ec: ExecutionContext): Stream[F, A]
    }

    implicit def streamExtOpsInstance[F[_]: Effect, A](s: Stream[F,A]): StreamExtOps[F, A] = new StreamExtOps[F,A] {
      def repeatWithInterval(interval: FiniteDuration)(implicit ec: ExecutionContext): Stream[F, A] =
        stream.repeatWithInterval(s,interval)


      def afterLastElement(f: Option[A] => F[Unit])(implicit ec: ExecutionContext): Stream[F, A] =
        stream.afterLastElement(s,f)
    }

  }


  object functional {

    // Awesomeness from this great article
    // see https://www.beyondthelines.net/programming/introduction-to-tagless-final/
    implicit class naturalTransformationOps[F[_],A](fa: F[A]) {
      def liftTo[G[_]](implicit trans: F ~> G): G[A] = trans(fa)
    }

    /**
      * Natural transformation of ConnectionIO[A] type to  Monad[A] type.
      *
      * When the monad is an effect type (Future, IO, Task etc...)
      * this essentially means execute the query (description) in a transaction and produce the result.
      */
    def connectionIOToMonad[F[_]: Monad](xa: Transactor[F]): ConnectionIO ~> F =
      new (ConnectionIO ~> F) {
        def apply[A](fa: ConnectionIO[A]): F[A] = xa.trans.apply(fa)
      }
  }
}
