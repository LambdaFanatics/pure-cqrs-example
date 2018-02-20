import java.util.UUID

import domain.events.{Event, PlantCreated}
import io.circe.generic.extras.{AutoDerivation, Configuration}
import io.circe.syntax._


/**
  * Example program demonstrating circe encoding with discriminator type.
  * Used in our application to serialize the typed events in the store.
  *
  * See circe project issue and jsFiddle of https://github.com/circe/circe/issues/726
  */
object CirceDiscriminatorTypeExample extends App {

  object Codec extends AutoDerivation {

    import io.circe._
    import io.circe.generic.extras.semiauto._

    implicit val configuration: Configuration = Configuration.default.withDiscriminator("type")
    implicit val eventEnc: Encoder[Event] = deriveEncoder[Event]
    implicit val eventDec: Decoder[Event] = deriveDecoder[Event]
  }


  import Codec._

  val encoded = {
     PlantCreated(UUID.randomUUID(), "FOO", "BAR").asInstanceOf[Event].asJson
  }

  println(s"Unwrapped Encoded: $encoded")
}
