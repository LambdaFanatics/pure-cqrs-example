package domain

import domain.commands.RawCommand


object validations {

  sealed trait ValidationError extends Product with Serializable

  // Generic command parse errors
  case class UnknownCommand(command: RawCommand) extends ValidationError
  case class InvalidCommandPayload(payload: String) extends ValidationError

  // Specific command validation errors
  case object CarAlreadyRegistered extends ValidationError
  case object CarNotRegistered extends ValidationError
  case object PartIsAlreadyMarked extends ValidationError
  case object PartIsNotMarked extends ValidationError
  case object CarHasDamagedParts extends ValidationError
}