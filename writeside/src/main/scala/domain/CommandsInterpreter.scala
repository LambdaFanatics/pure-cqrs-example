package domain

import cats.Functor
import cats.data.EitherT
import cats.effect.Effect
import io.circe.generic.auto._
import io.circe.syntax._

class CommandsInterpreter[F[_] : Effect](elog: EventLogAlgebra[F], v: ValidationAlgebra[F]) extends CommandsAlgebra[F] {


  // TODO decide if we will make these effect (F[_]) extensions...
  // Helper experiment for EitherT help operators
  implicit class EitherTLiftOp[G[_]: Functor, B](f: G[B]) {
    def liftF[A]: EitherT[G, A, B] = EitherT.liftF[G,A,B](f)
  }

  def create(name: String, country: String): F[Either[ValidationError, RawEvent]] = {
    val res: EitherT[F, ValidationError, RawEvent] = for {
      _   <- EitherT(v.checkPlantDoesNotExist(name))
      uid <- elog.generateUID().liftF
      _   <- v.put((uid, name)).liftF
      ev  <- elog.append(RawEvent(None, Event.toTypedEvent(PlantCreated(uid, name, country)).asJson)).liftF
    } yield ev
    res.value
  }

  def delete(id: PlantId): F[Either[ValidationError, RawEvent]] = {
    val res: EitherT[F, ValidationError, RawEvent] = for {
      _   <- EitherT(v.checkPlantExists(id))
      _   <- v.delete(id).liftF
      ev  <- elog.append(RawEvent(None, Event.toTypedEvent(PlantDeleted(id.value)).asJson)).liftF
    } yield ev
    res.value
  }
}

object CommandsInterpreter {
  def apply[F[_] : Effect](eventLog: EventLogAlgebra[F], validation: ValidationAlgebra[F]) =
    new CommandsInterpreter[F](eventLog, validation)
}