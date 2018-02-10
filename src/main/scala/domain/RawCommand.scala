package domain

import io.circe.Json

case class RawCommand(category: String, operation: String, payload: Json)


