package config

import cats.effect.Sync
import cats.implicits._
import pureconfig._
import pureconfig.error.ConfigReaderException

case class ApplicationConfig(db: DatabaseConfig)

object ApplicationConfig {
  /**
    * Loads the config using PureConfig.  If configuration is invalid we will
    * return an error.  This should halt the application from starting up.
    */
  def load[F[_]](namespace: String)(implicit E: Sync[F]): F[ApplicationConfig] =
    E.delay(loadConfig[ApplicationConfig](namespace)).flatMap {
      case Right(ok) => E.pure(ok)
      case Left(e) => E.raiseError(new ConfigReaderException[ApplicationConfig](e))
    }
}
