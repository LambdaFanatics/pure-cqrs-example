package domain

sealed trait ValidationError extends Product with Serializable

case class InvalidCommandError(command: RawCommand) extends ValidationError
