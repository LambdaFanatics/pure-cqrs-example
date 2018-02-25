package infrastructure.endpoint

import cats.effect.Effect
import domain.CommandsService
import org.http4s.{EntityDecoder, HttpService}
import org.http4s.dsl.Http4sDsl
import org.http4s.circe._
import io.circe.syntax._
import domain.commands.RawCommand

import domain.commands.codec._

class CommandEndpoints [F[_]: Effect] extends Http4sDsl[F]{

  import cats.implicits._

  implicit val commandDecoder: EntityDecoder[F, RawCommand] = jsonOf[F, RawCommand]

  def placeCommandEndpoint(service: CommandsService[F]): HttpService[F] =
    HttpService[F] {
      case req @ POST -> Root / "command" =>

        val action =  for {
          command <- req.as[RawCommand]
          res <- service.placeCommand(command)
        } yield res

        action.flatMap {
          case Right(_) => Ok()
          case Left(error) => Conflict(error.asJson)
        }

    }

  def endpoints(service: CommandsService[F]): HttpService[F] = placeCommandEndpoint(service)
}

object CommandEndpoints {
  def endpoints[F[_]: Effect](service: CommandsService[F]): HttpService[F] =
    new CommandEndpoints[F].endpoints(service)
}