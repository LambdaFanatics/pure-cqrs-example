package domain


object cars {

  sealed trait CarStatus extends Product with Serializable

  case object Unknown extends CarStatus

  case object Damaged extends CarStatus

  case object Repaired extends CarStatus

  object CarStatus {
    def apply(name: String): CarStatus = name match {
      case "UNKNOWN"  => Unknown
      case "DAMAGED"  => Damaged
      case "REPAIRED" => Repaired
    }

    def nameOf(status: CarStatus): String = status match {
      case Unknown  => "UNKNOWN"
      case Damaged  => "DAMAGED"
      case Repaired => "REPAIRED"
    }
  }


  case class Car(regPlate: String, model: String, status: CarStatus = Unknown)
}
