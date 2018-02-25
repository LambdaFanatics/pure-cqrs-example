package domain

import cats.data.EitherT
import cats.effect.Effect
import cats.implicits._
import domain.commands._
import domain.commands.codec._
import domain.validations.{InvalidCommandPayload, UnknownCommand, ValidationError}


class CommandsService[F[_] : Effect](commands: CommandsAlgebra[F]) {

  def placeCommand(cmd: RawCommand): F[Either[ValidationError, Unit]] = decodeAndExecute(cmd).value

  private def decodeAndExecute(cmd: RawCommand): EitherT[F, ValidationError, Unit]  = {
    cmd match {
      case RawCommand("car", "register", payload) =>
        EitherT.fromEither[F](payload
          .as[RegisterCar]
          .leftMap(_ => InvalidCommandPayload(payload.toString)))
          .flatMap(c => commands.registerCar(c.regPlate, c.model).map(_ => ()))


      case RawCommand("car", "repair", payload) =>
        EitherT.fromEither[F](payload
          .as[RepairCar]
          .leftMap(_ => InvalidCommandPayload(payload.toString)))
          .flatMap(c => commands.repairCar(c.regPlate).map(_ => ()))

      case RawCommand("part", "mark", payload) =>
        EitherT.fromEither[F](payload
          .as[MarkPart]
          .leftMap(_ => InvalidCommandPayload(payload.toString)))
          .flatMap(c => commands.markPart(c.regPlate, c.part).map(_ => ()))


      case RawCommand("part", "unmark", payload) =>
        EitherT.fromEither[F](payload
          .as[UnmarkPart]
          .leftMap(_ => InvalidCommandPayload(payload.toString)))
          .flatMap(c => commands.unmarkPart(c.regPlate, c.part).map(_ => ()))


      case RawCommand("part", "repair", payload) =>
        EitherT.fromEither[F](payload
          .as[RepairPart]
          .leftMap(_ => InvalidCommandPayload(payload.toString)))
          .flatMap(c => commands.repairPart(c.regPlate, c.part).map(_ => ()))

      case _ => EitherT.leftT(UnknownCommand(cmd))
    }

  }
}

object CommandsService {
  def apply[F[_] : Effect](commands: CommandsAlgebra[F]) = new CommandsService[F](commands)
}

