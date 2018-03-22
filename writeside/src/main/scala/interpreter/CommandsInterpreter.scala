package interpreter

import cats.data.EitherT
import cats.data.EitherT.liftF
import cats.effect.Effect
import domain.events._
import domain.validations._
import domain.{CommandsAlgebra, EventLogAlgebra, ValidationAlgebra}

class CommandsInterpreter[F[_] : Effect](elog: EventLogAlgebra[F], v: ValidationAlgebra[F]) extends CommandsAlgebra[F] {

  def registerCar(regPlate: String, model: String): EitherT[F, ValidationError, Event] =
    for {
      _ <- EitherT(v.attemptToRegisterCar(regPlate))
      event = CarRegistered(regPlate, model)
      ev <- liftF(elog.append(event))
    } yield ev

  def repairCar(regPlate: String): EitherT[F, ValidationError, Event] =
    for {
      _ <- EitherT(v.attemptToRepairCar(regPlate))
      ev <- liftF(elog.append(CarRepaired(regPlate)))
    } yield ev

  def markPart(regPlate: String, part: String): EitherT[F, ValidationError, Event] =
    for {
      _ <- EitherT(v.attemptToMarkPart(regPlate, part))
      event = PartMarked(regPlate, part)
      ev <- liftF(elog.append(event))
    } yield ev

  def unmarkPart(regPlate: String, part: String): EitherT[F, ValidationError, Event] =
    for {
      _ <- EitherT(v.attemptToUnmarkPart(regPlate, part))
      ev <- liftF(elog.append(PartUnmarked(regPlate, part)))
    } yield ev

  def repairPart(regPlate: String, part: String): EitherT[F, ValidationError, Event] =
    for {
      _ <- EitherT(v.attemptToRepairPart(regPlate, part))
      ev <- liftF(elog.append(PartRepaired(regPlate, part)))
    } yield ev
}