package infrastructure.repository.inmemory

import cats.Applicative
import domain._

import scala.collection.concurrent.TrieMap
import cats.implicits._


class ValidationInMemoryInterpreter[F[_]: Applicative] extends ValidationAlgebra[F] {


  // FIXME: Awfull implementation and API reimplement with something else (redis in memory, some other cache, or doobie or whatever...)
  val cache = new TrieMap[PlantId, String]


  def put(p: PlantDescription): F[Unit] = {
    //FIXME: Atomicity ...
    cache.put(p._1 , p._2)
    ()
  }.pure[F]


  def delete(id: PlantId): F[Unit] = {
    cache.remove(id)
    ()
  }.pure[F]

  def checkPlantDoesNotExist(name: String): F[Either[PlantAlreadyExists.type, Unit]] =
    cache.find(_._2 == name).toLeft(()).leftMap(_ => PlantAlreadyExists).pure[F]



  def checkPlantExists(id: PlantId): F[Either[PlantDoesNotExist.type, PlantDescription]] =
    cache.find(_._1 == id).toRight(PlantDoesNotExist).pure[F]

}

object ValidationInMemoryInterpreter {
  def apply[F[_]: Applicative] = new ValidationInMemoryInterpreter[F]
}