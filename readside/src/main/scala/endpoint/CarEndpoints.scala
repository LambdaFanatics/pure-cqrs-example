package endpoint

import cats.Monad
import cats.effect.Effect
import io.circe.syntax._
import io.circe.generic.auto._
import org.http4s.circe._
import org.http4s.HttpService
import org.http4s.dsl.Http4sDsl
import service.CarService
import cats.implicits._

class CarEndpoints[G[_] : Monad, F[_] : Effect](service: CarService[G, F]) extends Http4sDsl[F] {

  def getCars: HttpService[F] = HttpService[F] {
    case GET -> Root / "cars" => service.getCars.flatMap(cars => Ok(cars.asJson))

  }

  def endpoints(): HttpService [F] = getCars
}