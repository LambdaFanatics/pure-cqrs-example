package domain

sealed trait ValidationError extends Product with Serializable

case class UnknownCommandError(command: RawCommand) extends ValidationError
case class InvalidCommandPayload(payload: String) extends ValidationError
