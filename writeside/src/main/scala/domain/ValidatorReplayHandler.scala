package domain

import cats.effect.Effect
import cats.implicits._
import domain.events._
import domain.validations.ValidationError
import fs2._

class ValidatorReplayHandler[F[_] : Effect](elog: EventLogAlgebra[F], v: ValidationAlgebra[F]) {

  def initializeState(): Stream[F, Unit] =
    elog.consume("validation-handler", SeekBeginning, closeOnEnd = true)
      .evalMap(ev =>
        updateState(ev).map {
          //TODO log here
          case Left(err) => println(s"Error while recreating validation state event: $ev caused $err! POSSIBLE VALIDATION STORE INCONSISTENCY!")
          case _ => ()
        }).drain

  private def updateState(ev: Event): F[Either[ValidationError, Unit]] = ev match {
    case CarRegistered(plate, _) => v.attemptToRegisterCar(plate)
    case CarRepaired(plate) => v.attemptToRepairCar(plate)
    case PartMarked(plate, part) => v.attemptToMarkPart(plate, part)
    case PartUnmarked(plate, part) => v.attemptToUnmarkPart(plate, part)
    case PartRepaired(plate, part) => v.attemptToRepairPart(plate, part)
  }
}