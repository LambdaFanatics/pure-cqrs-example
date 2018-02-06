package infrastructure.repository
package inmemory

import cats.Applicative
import cats.implicits._
import domain.{Plant, PlantId, PlantRepoAlgebra}

import scala.collection.concurrent.TrieMap
import scala.util.Random


class PlantRepoInMemoryInterpreter[F[_]: Applicative] extends PlantRepoAlgebra[F] {

  private val cache = new TrieMap[PlantId, Plant]


  def create(plant: Plant): F[Plant] = {
    val id = Random.alphanumeric.take(10).mkString("")
    val toSave = plant.copy(id = id)
    cache += (PlantId(id)  -> toSave)
    toSave.pure[F]
  }

  def delete(id: PlantId): F[Option[Plant]] = cache.remove(id).pure[F]

  def get(id: PlantId): F[Option[Plant]] = cache.get(id).pure[F]

  def findByName(name: String): F[Option[Plant]] = cache.values.find(p => p.name == name).pure[F]

  def list(): F[List[Plant]] = cache.values.toList.pure[F]
}


object PlantRepoInMemoryInterpreter {
  def apply[F[_]: Applicative] = new PlantRepoInMemoryInterpreter[F]
}