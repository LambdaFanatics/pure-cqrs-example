package domain


import cats.Monad
import cats.data.EitherT
import cats.implicits._
import domain.commands._
import domain.commands.codec._
import domain.validations.{InvalidCommandPayload, UnknownCommand, ValidationError}


class CommandsService[F[_]: Monad](commands: CommandsAlgebra[F]) {

  def placeCommand(cmd: RawCommand): F[Either[ValidationError, Command]] = {
    val res = for {
      cmd <- EitherT.fromEither[F](matchCommand(cmd))
      res <- tryExecute(cmd)
    } yield res
    res.value
  }

  private def tryExecute(matched: Command): EitherT[F, ValidationError, Command]  = matched match {
    case cmd@CreatePlant(name, country) =>
      EitherT(commands.create(name, country)).map(_ => cmd)

    case cmd@DeletePlant(id) =>
      EitherT(commands.delete(id)).map(_ => cmd)
  }

  private def matchCommand(cmd: RawCommand): Either[ValidationError, Command] =
    cmd match {
      case RawCommand("plant", "create", payload) =>
        payload.as[CreatePlant].leftMap(_ => InvalidCommandPayload(payload.toString))

      case RawCommand("plant", "delete", payload) => {
        payload.as[DeletePlant].leftMap(_ => InvalidCommandPayload(payload.toString)) }

      case _ => UnknownCommand(cmd).asLeft
    }
}

object CommandsService {
  def apply[F[_]: Monad](commands: CommandsAlgebra[F]) = new CommandsService[F](commands)
}

