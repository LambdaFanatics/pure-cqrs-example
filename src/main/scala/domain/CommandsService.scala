package domain

import cats.Monad
import cats.data.EitherT
import cats.syntax.either._

import io.circe.generic.auto._

class CommandsService[F[_] : Monad] {

  import CommandsService._

  def placeCommand(cmd: RawCommand): EitherT[F, ValidationError, Command] =
    for {
      cmd <- EitherT.fromEither[F](matchCommand(cmd))
      res <- tryExecute(cmd)
    } yield res

  private def matchCommand(cmd: RawCommand): Either[ValidationError, Command] =
    cmd match {
      case RawCommand("plant", "create", payload) =>
        payload.as[CreatePlant].leftMap(_ => InvalidCommandError(cmd))



//      case RawCommand("plant", "delete", _) =>
//        DeletePlant("1").asRight

      case _ => InvalidCommandError(cmd).asLeft
    }


  private def tryExecute(matched: Command): EitherT[F, ValidationError, Command]  = matched match {
    case c@_ => EitherT.rightT[F, ValidationError](c)
  }
}

object CommandsService {

  sealed trait Command extends Product with Serializable

  case class CreatePlant(name: String, country: String) extends Command
  case class DeletePlant(id: String) extends Command

  def apply[F[_]: Monad]() = new CommandsService[F]

}
