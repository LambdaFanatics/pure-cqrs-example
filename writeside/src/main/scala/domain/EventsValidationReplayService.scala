package domain

import cats.effect.Effect
import cats.implicits._
import domain.events._
import domain.validations.ValidationError
import fs2._

class EventsValidationReplayService[F[_] : Effect](v: ValidationAlgebra[F], elog: EventLogAlgebra[F]) {

  def initializeState: Stream[F, Unit] =
    elog.consume().evalMap(ev => updateState(ev).map {
      //TODO log here
      case Left(err) => println(s"Error while recreating validation state event: $ev caused $err! POSSIBLE VALIDATION STORE INCONSISTENCY!")
      case _ => println(s"Replayed event: $ev")
    }).drain

  private def updateState(ev: Event): F[Either[ValidationError, Unit]] = ev match {
    case CarRegistered(plate, _) => v.attemptToRegisterCar(plate)
    case CarRepaired(plate) => v.attemptToRepairCar(plate)
    case PartMarked(plate, part) => v.attemptToMarkPart(plate, part)
    case PartUnmarked(plate, part) => v.attemptToUnmarkPart(plate, part)
    case PartRepaired(plate, part) => v.attemptToRepairPart(plate, part)
  }
}

object EventsValidationReplayService {
  def apply[F[_] : Effect](v: ValidationAlgebra[F], elog: EventLogAlgebra[F] ) = new EventsValidationReplayService[F](v, elog)
}
