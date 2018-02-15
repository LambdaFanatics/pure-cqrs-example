package domain

import cats.data.EitherT
import cats.data.EitherT.liftF
import cats.effect.Effect

class CommandsInterpreter[F[_] : Effect](elog: EventLogAlgebra[F], v: ValidationAlgebra[F]) extends CommandsAlgebra[F] {

  def create(name: String, country: String): F[Either[ValidationError, RawEvent]] = {
    val res: EitherT[F, ValidationError, RawEvent] = for {
      _   <- EitherT { v.checkPlantDoesNotExist(name) }
      uid <- liftF(elog.generateUID())
      _   <- liftF(v.put((uid, name)))
      ev  <- liftF(elog.append(RawEvent(None, s"PLANT_CREATED $uid $name $country")))
    } yield ev
    res.value
  }

  def delete(id: PlantId): F[Either[ValidationError, RawEvent]] = {
    val res: EitherT[F, ValidationError, RawEvent] = for {
      _   <- EitherT(v.checkPlantExists(id))
      _   <- liftF(v.delete(id))
      ev  <- liftF(elog.append(RawEvent(None, s"PLANT_DELETED $id.value")))
    } yield ev
    res.value
  }
}

object CommandsInterpreter {
  def apply[F[_] : Effect](eventLog: EventLogAlgebra[F], validation: ValidationAlgebra[F]) =
    new CommandsInterpreter[F](eventLog, validation)
}