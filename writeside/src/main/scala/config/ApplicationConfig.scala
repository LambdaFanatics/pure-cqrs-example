package config

import cats.effect.Effect
import cats.implicits._
import pureconfig.error.ConfigReaderException
import pureconfig._

case class ApplicationConfig(db: DatabaseConfig)

object ApplicationConfig {


  /**
    * Loads the pet store config using PureConfig.  If configuration is invalid we will
    * return an error.  This should halt the application from starting up.
    */
  def load[F[_]](implicit E: Effect[F]): F[ApplicationConfig] =
    E.delay(loadConfig[ApplicationConfig]("pure-cqrs")).flatMap {
      case Right(ok) => E.pure(ok)
      case Left(e) => E.raiseError(new ConfigReaderException[ApplicationConfig](e))
    }
}
