package domain

import java.util.UUID
import io.circe.Json
import io.circe.generic.extras.AutoDerivation

object commands {

  case class RawCommand(category: String, operation: String, payload: Json)

  sealed trait Command extends Product with Serializable
  final case class RegisterCar(regPlate: String, model: String) extends Command
  final case class AddDamagedPart(carId: UUID, name: String) extends Command
  final case class RemoveDamagedPart(id: UUID) extends Command
  final case class RepairDamagedPart(id: UUID) extends Command
  final case class RepairCar(id: UUID) extends Command





  // See issue and jsFiddle of https://github.com/circe/circe/issues/726
  object codec extends AutoDerivation {

    import io.circe.generic.extras.semiauto._
    import io.circe.generic.extras.Configuration
    import io.circe.{Decoder, Encoder}

    implicit val configuration: Configuration = Configuration.default
    implicit val commandEnc: Encoder[Command] = deriveEncoder[Command]
    implicit val commandDec: Decoder[Command] = deriveDecoder[Command]

    implicit val rawCommandDecoder: Decoder[RawCommand] = deriveDecoder[RawCommand]
  }
}