package domain

sealed trait ValidationError extends Product with Serializable


// Generic command parse errors
case class UnknownCommand(command: RawCommand) extends ValidationError
case class InvalidCommandPayload(payload: String) extends ValidationError


// Specific command validation errors
case object PlantAlreadyExists extends ValidationError
case object PlantDoesNotExist extends ValidationError
