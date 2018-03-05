package domain


object cars {

  sealed trait DamageStatus extends Product with Serializable

  case object Unknown extends DamageStatus

  case object Damaged extends DamageStatus

  case object Repaired extends DamageStatus

  object DamageStatus {
    def apply(name: String): DamageStatus = name match {
      case "UNKNOWN"  => Unknown
      case "DAMAGED"  => Damaged
      case "REPAIRED" => Repaired
    }

    def nameOf(status: DamageStatus): String = status match {
      case Unknown  => "UNKNOWN"
      case Damaged  => "DAMAGED"
      case Repaired => "REPAIRED"
    }
  }

  case class Car(regPlate: String, model: String, status: DamageStatus = Unknown)

  case class CarPart(name: String, carPlate: String, status: DamageStatus = Damaged)
}
