package domain

import java.util.UUID

import io.circe.Json

case class RawEvent(id: Option[Long], payload: Json)

case class TypedEvent(name: String, payload: Event)


sealed trait Event

case class PlantCreated(id: UUID, name: String, country: String) extends Event

case class PlantDeleted(id: UUID) extends Event

object Event  {
  def toTypedEvent(ev: Event): TypedEvent = TypedEvent(ev.getClass.getName, ev)
}

