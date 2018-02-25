package domain


import domain.validations._


trait ValidationAlgebra[F[_]] {

  def checkThatCarIsRegistered(regPlate: String): F[CarNotRegistered.type Either Unit]

  def checkThatCarIsNotRegistered(regPlate: String): F[CarAlreadyRegistered.type Either Unit]

  def checkThatCarHasNoDamages(regPlate: String): F[CarHasDamagedParts.type Either Unit]

  def checkThatPartIsMarked(regPlate: String, part: String): F[PartIsNotMarked.type Either Unit]

  def checkThatPartIsNotMarked(regPlate: String, part: String): F[PartIsAlreadyMarked.type Either Unit]

}
