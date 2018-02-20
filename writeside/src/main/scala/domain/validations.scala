package domain

import io.circe.generic.extras.AutoDerivation
import domain.commands.RawCommand


object validations {

  sealed trait ValidationError extends Product with Serializable

  // Generic command parse errors
  case class UnknownCommand(command: RawCommand) extends ValidationError
  case class InvalidCommandPayload(payload: String) extends ValidationError

  // Specific command validation errors
  case object PlantAlreadyExists extends ValidationError
  case object PlantDoesNotExist extends ValidationError


    // See issue and jsFiddle of https://github.com/circe/circe/issues/726
    object codec extends AutoDerivation {

      import io.circe.generic.extras.semiauto._
      import io.circe.generic.extras.Configuration

      implicit val configuration: Configuration = Configuration.default
      implicit val validationEnc = deriveEncoder[ValidationError]
      implicit val validationDec = deriveDecoder[ValidationError]
    }
}