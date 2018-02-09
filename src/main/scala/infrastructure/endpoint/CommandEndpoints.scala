package infrastructure.endpoint

import cats.effect.Effect
import io.circe.generic.auto._
import io.circe.syntax._
import domain.RawCommand
import org.http4s.{EntityDecoder, HttpService}
import org.http4s.dsl.Http4sDsl
import org.http4s.circe._


class CommandEndpoints [F[_]: Effect] extends Http4sDsl[F]{

  import cats.implicits._

  implicit val commandDecoder: EntityDecoder[F, RawCommand] = jsonOf[F, RawCommand]

  def placeCommandEndpoint(): HttpService[F] =
    HttpService[F] {
      case req @ POST -> Root / "command" =>
        for {
          command <- req.as[RawCommand]
          resp <- Ok(command.asJson)
        } yield resp
    }

  def endpoints(): HttpService[F] = placeCommandEndpoint()
}

object CommandEndpoints {
  def endpoints[F[_]: Effect](): HttpService[F] =
    new CommandEndpoints[F].endpoints()
}