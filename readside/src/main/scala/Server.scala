import cats.effect.{Effect, IO}
import config.{ApplicationConfig, DatabaseConfig}
import domain.CarsStoreEventHandler
import fs2._
import interpreter.StoreInterpreter
import interpreter.doobie.{CarPartStoreDoobieInterpreter, CarStoreDoobieInterpreter, EventLogDoobieInterpreter}
import org.http4s.server.blaze.BlazeBuilder
import utils.functional.connectionIOToMonad
import doobie.implicits._  // This resolves Monad[ConnectionIO] ambiguous implicits somehow.

import scala.concurrent.ExecutionContext.Implicits.global

object Server extends ReadSideServer[IO]

class ReadSideServer[F[_] : Effect] extends StreamApp[F] {


  def stream(args: List[String], requestShutdown: F[Unit]): Stream[F, StreamApp.ExitCode] =

    // TODO: We need a module in order to define all this dependency injection...
    for {
      conf <- Stream.eval(ApplicationConfig.load[F]("read-side-server"))
      xa <- Stream.eval(DatabaseConfig.dbTransactor[F](conf.db))
      eventLog = EventLogDoobieInterpreter(xa)
      execDbAction = connectionIOToMonad[F](xa)
      carStore = CarStoreDoobieInterpreter()
      partStore = CarPartStoreDoobieInterpreter()
      store = StoreInterpreter(carStore, partStore)
      storeEventHandler =  {
        implicit val trans = execDbAction   // FIXME Confusing implicits declaration
        CarsStoreEventHandler(store,eventLog)
      }

      exitCode <- BlazeBuilder[F]  // Start the server
        .bindHttp(8081)
        .serve
        .concurrently(storeEventHandler.process()) //Start the store event handler in the background
    } yield exitCode


}