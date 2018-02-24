package domain


import cats.Monad
import cats.data.EitherT
import cats.implicits._
import domain.commands._
import domain.commands.codec._
import domain.validations.{InvalidCommandPayload, UnknownCommand, ValidationError}


class CommandsService[F[_] : Monad](commands: CommandsAlgebra[F]) {

  def placeCommand(cmd: RawCommand): F[Either[ValidationError, Command]] = {
    val res = for {
      cmd <- EitherT.fromEither[F](matchCommand(cmd))
      res <- tryExecute(cmd)
    } yield res
    res.value
  }

  private def tryExecute(matched: Command): EitherT[F, ValidationError, Command] =
    matched match {
      case cmd@RegisterCar(regPlate, model) =>
        EitherT(commands.registerCar(regPlate, model)).map(_ => cmd)

      case cmd@RepairCar(id) =>
        EitherT(commands.repairCar(id)).map(_ => cmd)

      case cmd@AddDamagedPart(carId, name) =>
        EitherT(commands.addDamagedPart(carId, name)).map(_ => cmd)

      case cmd@RemoveDamagedPart(id) =>
        EitherT(commands.removeDamagedPart(id)).map(_ => cmd)

      case cmd@RepairDamagedPart(id) =>
        EitherT(commands.repairDamagedPart(id)).map(_ => cmd)
    }

  private def matchCommand(cmd: RawCommand): Either[ValidationError, Command] =
    cmd match {
      case RawCommand("car", "register", payload) =>
        payload.as[RegisterCar].leftMap(_ => InvalidCommandPayload(payload.toString))

      case RawCommand("car", "repair", payload) =>
        payload.as[RepairCar].leftMap(_ => InvalidCommandPayload(payload.toString))

      case RawCommand("part", "add", payload) =>
        payload.as[AddDamagedPart].leftMap(_ => InvalidCommandPayload(payload.toString))

      case RawCommand("part", "remove", payload) =>
        payload.as[RemoveDamagedPart].leftMap(_ => InvalidCommandPayload(payload.toString))

      case RawCommand("part", "repair", payload) =>
        payload.as[RepairDamagedPart].leftMap(_ => InvalidCommandPayload(payload.toString))

      case _ => UnknownCommand(cmd).asLeft
    }
}

object CommandsService {
  def apply[F[_] : Monad](commands: CommandsAlgebra[F]) = new CommandsService[F](commands)
}

