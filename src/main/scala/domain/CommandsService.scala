package domain

import java.util.UUID

import cats.Monad
import cats.data.EitherT
import io.circe.generic.auto._
import cats.implicits._

class CommandsService[F[_]: Monad](commands: CommandsAlgebra[F]) {

  import CommandsService._

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

  sealed trait Command extends Product with Serializable

  final case class CreatePlant(name: String, country: String) extends Command
  final case class DeletePlant(id: UUID) extends Command

  def apply[F[_]: Monad](commands: CommandsAlgebra[F]) = new CommandsService[F](commands)

}
