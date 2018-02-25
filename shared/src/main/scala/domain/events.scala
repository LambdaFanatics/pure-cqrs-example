package domain


import io.circe.generic.extras.AutoDerivation

object events  {
  sealed trait Event extends Product with Serializable
  final case class CarRegistered(regPlate: String, model: String) extends Event
  final case class CarRepaired(regPlate: String) extends Event
  final case class PartMarked(regPlate: String,  part: String) extends Event
  final case class PartUnmarked(regPlate: String, part: String) extends Event
  final case class PartRepaired(regPlate: String, part: String) extends Event

  /**
    * Here we provide a semi auto circe codec configuration b/c we need
    * the json type discriminator to deserialize the classes from the log
    * */
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