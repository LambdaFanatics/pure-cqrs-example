package interpreter.memory

import java.util.UUID

import cats.Applicative
import cats.implicits._
import domain.{Plant, PlantId, PlantStoreAlgebra}

import scala.collection.concurrent.TrieMap


class PlantStoreInMemoryInterpreter[F[_]: Applicative] extends PlantStoreAlgebra[F] {

  private val cache = new TrieMap[PlantId, Plant]


  def create(plant: Plant): F[Plant] = {
    val id = UUID.randomUUID()
    val toSave = plant.copy(id = id)
    cache += (PlantId(id)  -> toSave)
    toSave.pure[F]
  }

  def delete(id: PlantId): F[Option[Plant]] = cache.remove(id).pure[F]

  def get(id: PlantId): F[Option[Plant]] = cache.get(id).pure[F]

  def findByName(name: String): F[Option[Plant]] = cache.values.find(p => p.name == name).pure[F]

  def list(): F[List[Plant]] = cache.values.toList.pure[F]
}


object PlantStoreInMemoryInterpreter {
  def apply[F[_]: Applicative]() = new PlantStoreInMemoryInterpreter[F]
}