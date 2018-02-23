package domain

import java.util.UUID

import io.circe.generic.extras.AutoDerivation

object events  {



  sealed trait Event extends Product with Serializable
  case class PlantCreated(id: UUID, name: String, country: String) extends Event
  case class PlantDeleted(id: UUID) extends Event

  // See issue and jsFiddle of https://github.com/circe/circe/issues/726
  object codec extends AutoDerivation {
    import io.circe._
    import io.circe.generic.extras.Configuration
    import io.circe.generic.extras.semiauto._

    implicit val configuration: Configuration = Configuration.default.withDiscriminator("type")

    implicit val eventEnc: Encoder[Event] = deriveEncoder[Event]
    implicit val eventDec: Decoder[Event] = deriveDecoder[Event]
  }
}