package domain

import cats.Monad
import cats.data.EitherT
import cats.implicits._
import io.circe.generic.auto._

class CommandsService[F[_] : Monad](eventLog: EventLogAlgebra[F]) {

  import CommandsService._

  def placeCommand(cmd: RawCommand): EitherT[F, ValidationError, Command] =
    for {
      cmd <- EitherT.fromEither[F](matchCommand(cmd))
      res <- tryExecute(cmd)
    } yield res

  private def tryExecute(matched: Command): EitherT[F, ValidationError, Command]  = matched match {
    case cmd@CreatePlant(_,_) =>
      EitherT.liftF(eventLog.append(Event(None, cmd.toString))).map(_ => cmd)

    case cmd@DeletePlant(_) =>
      EitherT.liftF(eventLog.append(Event(None, cmd.toString))).map(_ => cmd)
  }

  private def matchCommand(cmd: RawCommand): Either[ValidationError, Command] =
    cmd match {
      case RawCommand("plant", "create", payload) =>
        payload.as[CreatePlant].leftMap(_ => InvalidCommandPayload(payload.toString))

      case RawCommand("plant", "delete", payload) => {
        payload.as[CreatePlant].leftMap(_ => InvalidCommandPayload(payload.toString)) }

      case _ => UnknownCommandError(cmd).asLeft
    }
}

object CommandsService {

  sealed trait Command extends Product with Serializable

  case class CreatePlant(name: String, country: String) extends Command
  case class DeletePlant(id: String) extends Command

  def apply[F[_]: Monad](eventsLog: EventLogAlgebra[F]) = new CommandsService[F](eventsLog)

}
