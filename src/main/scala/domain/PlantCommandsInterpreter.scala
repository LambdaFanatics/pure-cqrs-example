package domain

import cats.effect.Effect

import cats.syntax.functor._

class PlantCommandsInterpreter[F[_]:Effect](eventLog: EventLogAlgebra[F]) extends PlantCommandsAlgebra[F] {
  def create(name: String, country: String): F[Unit] = eventLog.append(Event(None, s"PLANT_CREATED $name $country")).as(())

  def delete(id: PlantId): F[Unit] = eventLog.append(Event(None, s"PLANT_DELETED $id")).as(())
}

object PlantCommandsInterpreter {
  def apply[F[_]: Effect](eventLog: EventLogAlgebra[F]) = new PlantCommandsInterpreter[F](eventLog)
}
