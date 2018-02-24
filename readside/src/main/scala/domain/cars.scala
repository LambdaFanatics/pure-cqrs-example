package domain

import java.util.UUID

object cars {

  sealed trait CarStatus extends Product with Serializable

  case object Damaged extends CarStatus

  case object Repaired extends CarStatus

  object CarStatus {
    def apply(name: String): CarStatus = name match {
      case "Damaged"  => Damaged
      case "Repaired" => Repaired
    }

    def nameOf(status: CarStatus): String = status match {
      case Damaged  => "Damaged"
      case Repaired => "Repaired"
    }
  }

  case class DamagedCar(id: UUID, regPlate: String, status: CarStatus)


}
