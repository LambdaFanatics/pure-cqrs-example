package domain
//
//import cats.syntax.applicative._
//import cats.syntax.either._
import cats.Monad
import cats.data.EitherT

//class CommandService[F[_]](repository: PlantStoreAlgebra[F],
//                           validation: PlantValidationAlgebra[F],
//                           commands: PlantCommandsAlgebra[F],
//                           eventLog: EventLogAlgebra[F]) {

class CommandService[F[_]: Monad] {

  import CommandService._

  def placeCommand(cmd: RawCommand)  = (for {
    cmd <- EitherT(matchCommand(cmd))
  } yield cmd).value

  def matchCommand(cmd: RawCommand): F[Either[ValidationError, Command]] = ???
//    cmd match {
//      case RawCommand("plant", "create", _) => CreatePlant("ALINO", "GREECE").asRight[ValidationError].pure[F]
//      case _ => InvalidCommandError.asLeft[Command].pure[F]
//    }
}

object CommandService {

  sealed trait Command extends Product with Serializable

  case class CreatePlant(name: String, country: String) extends Command

}
