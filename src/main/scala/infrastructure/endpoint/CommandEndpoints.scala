package infrastructure.endpoint

import cats.effect.Effect
import io.circe.generic.auto._
import io.circe.syntax._
import domain.{CommandsService, RawCommand}
import org.http4s.{EntityDecoder, HttpService}
import org.http4s.dsl.Http4sDsl
import org.http4s.circe._


class CommandEndpoints [F[_]: Effect] extends Http4sDsl[F]{

  import cats.implicits._

  implicit val commandDecoder: EntityDecoder[F, RawCommand] = jsonOf[F, RawCommand]

  def placeCommandEndpoint(service: CommandsService[F]): HttpService[F] =
    HttpService[F] {
      case req @ POST -> Root / "command" =>
        for {
          command <- req.as[RawCommand]
          res <- service.placeCommand(command).value
          resp <- Ok(res.asJson)
        } yield resp
    }

  def endpoints(service: CommandsService[F]): HttpService[F] = placeCommandEndpoint(service)
}

object CommandEndpoints {
  def endpoints[F[_]: Effect](service: CommandsService[F]): HttpService[F] =
    new CommandEndpoints[F].endpoints(service)
}