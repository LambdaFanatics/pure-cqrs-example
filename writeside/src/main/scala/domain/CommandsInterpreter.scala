package domain

import java.util.UUID

import cats.Functor
import cats.data.EitherT
import cats.effect.Effect
import domain.events._
import domain.validations._

// FIXME redesign validations
class CommandsInterpreter[F[_] : Effect](elog: EventLogAlgebra[F], v: ValidationAlgebra[F]) extends CommandsAlgebra[F] {

  // TODO decide if we will make these effect (F[_]) extension...
  // Helper experiment for EitherT help operators
  implicit class EitherTLiftOp[G[_]: Functor, B](f: G[B]) {
    def liftF[A]: EitherT[G, A, B] = EitherT.liftF[G,A,B](f)
  }


  def registerCar(regPlate: String, model: String): F[Either[ValidationError, Event]] = {
    val res: EitherT[F, ValidationError, Event] = for {
      uid <- elog.generateUID().liftF
      event = CarRegistered(uid, regPlate, model)
      ev <- elog.append(event).liftF
    } yield ev
    res.value
  }


  def repairCar(id: UUID): F[Either[ValidationError, Event]] = {
    val res: EitherT[F, ValidationError, Event] = for {
      ev <- elog.append(CarRepaired(id)).liftF
    } yield ev
    res.value
  }

  def addDamagedPart(carId: UUID, name: String): F[Either[ValidationError, Event]] = {
    val res: EitherT[F, ValidationError, Event] = for {
      uid <- elog.generateUID().liftF
      event = DamagedPartAdded(uid, carId, name)
      ev <- elog.append(event).liftF
    } yield ev
    res.value
  }

  def removeDamagedPart(id: UUID): F[Either[ValidationError, Event]] = {
    val res: EitherT[F, ValidationError, Event] = for {
      ev <- elog.append(DamagedPartRemoved(id)).liftF
    } yield ev
    res.value
  }

  def repairDamagedPart(id: UUID): F[Either[ValidationError, Event]] = {
    val res: EitherT[F, ValidationError, Event] = for {
      ev <- elog.append(DamagedPartRepaired(id)).liftF
    } yield ev
    res.value
  }
}


//
//  def create(name: String, country: String): F[Either[ValidationError, Event]] = {
//    val res: EitherT[F, ValidationError, Event] = for {
//
////      _   <- EitherT(v.checkPlantDoesNotExist(name))
//      uid <- elog.generateUID().liftF
////      _   <- v.put((uid, name)).liftF
//      event = PlantCreated(uid, name, country)
//      ev  <- elog.append(event).liftF
//    } yield ev
//    res.value
//  }
//
//  def delete(id: UUID): F[Either[ValidationError, Event]] = {
//    val res: EitherT[F, ValidationError, Event] = for {
//      _   <- EitherT(v.checkPlantExists(id))
//      _   <- v.delete(id).liftF
//      ev  <- elog.append(PlantDeleted(id)).liftF
//    } yield ev
//    res.value
//  }


object CommandsInterpreter {
  def apply[F[_] : Effect](eventLog: EventLogAlgebra[F], validation: ValidationAlgebra[F]) =
    new CommandsInterpreter[F](eventLog, validation)
}