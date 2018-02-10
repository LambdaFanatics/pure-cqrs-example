package domain

import cats.Monad
import cats.data.EitherT

class CommandsService[F[_] : Monad] {

  import CommandsService._

  def placeCommand(cmd: RawCommand): EitherT[F, ValidationError, Command] =
    for {
      cmd <- matchCommand(cmd)
    } yield cmd

  private def matchCommand(cmd: RawCommand): EitherT[F, ValidationError, Command] =
    cmd match {
      case RawCommand("plant", "create", _) =>
        EitherT.rightT[F, ValidationError](CreatePlant("ALINO", "GREECE"))
      case _ => EitherT.leftT[F, Command](InvalidCommandError(cmd))
    }
}

object CommandsService {

  sealed trait Command extends Product with Serializable

  case class CreatePlant(name: String, country: String) extends Command

  def apply[F[_]: Monad]() = new CommandsService[F]

}
