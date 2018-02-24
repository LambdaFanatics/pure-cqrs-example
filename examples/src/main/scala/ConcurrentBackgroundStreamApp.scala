import cats.effect.{Effect, IO}
import fs2._
import fs2.async.mutable.Signal

import scala.concurrent.ExecutionContext.Implicits.global
import scala.io.StdIn

object ConcurrentBackgroundStreamApp extends App {

  def log[F[_] : Effect, A](prefix: String): Pipe[F, A, A] = _.evalMap(a => Effect[F].delay {
    println(s"[$prefix - ${Thread.currentThread.getName}] $a ")
    a
  })

  def backgroundStream[F[_] : Effect](halt: Signal[F, Boolean]) =
    Stream(1, 2).repeat.covary[F].interruptWhen(halt).through(log("background"))

  val halt = async.signalOf[IO, Boolean](false).unsafeRunSync()

  async.unsafeRunAsync(backgroundStream(halt).compile.last) {
    case Right(a) => IO(println(a))
    case Left(_) => IO(println("error"))
  }
  println("Press enter to stop...")
  StdIn.readLine()
  halt.set(true).unsafeRunSync()
  StdIn.readLine()

}


