package domain

sealed trait ValidationError extends Product with Serializable

case object InvalidCommandError extends ValidationError
