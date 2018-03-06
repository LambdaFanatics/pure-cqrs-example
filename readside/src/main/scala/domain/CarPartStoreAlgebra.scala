package domain

import domain.parts.CarPart


trait CarPartStoreAlgebra[F[_]] {
  def create(part: CarPart): F[CarPart]

  def update(part: CarPart): F[Option[CarPart]]

  def modify(regPlate: String, name: String)(f: CarPart => CarPart): F[Option[CarPart]]

  def get(name: String, carPlate: String): F[Option[CarPart]]

  def delete(name: String, carPlate: String): F[Option[CarPart]]

  def list(): F[List[CarPart]]
}
