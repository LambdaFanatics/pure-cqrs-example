package domain


import domain.validations._


trait ValidationAlgebra[F[_]] {

  def attemptToRegisterCar(regPlate: String): F[ValidationError Either Unit]

  def attemptToRepairCar(regPlate: String): F[ValidationError Either Unit]

  def attemptToUnmarkPart(regPlate: String, part: String): F[ValidationError Either Unit]

  def attemptToMarkPart(regPlate: String, part: String): F[ValidationError Either Unit]

  def attemptToRepairPart(regPlate: String, part: String): F[ValidationError Either Unit]

}
