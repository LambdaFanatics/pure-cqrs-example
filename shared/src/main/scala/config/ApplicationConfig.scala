package config

import cats.effect.Sync
import cats.implicits._
import pureconfig._
import pureconfig.error.ConfigReaderException

case class ApplicationConfig(db: DatabaseConfig)

object ApplicationConfig {
  /**
    * Loads the config using PureConfig.  If configuration is invalid we will
    * return an error. This should halt the application from starting up.
    */
  def load[F[_]](namespace: String)(implicit S: Sync[F]): F[ApplicationConfig] =
    S.delay(loadConfig[ApplicationConfig](namespace)).flatMap {
      case Right(ok) => S.pure(ok)
      case Left(e) => S.raiseError(new ConfigReaderException[ApplicationConfig](e))
    }
}
