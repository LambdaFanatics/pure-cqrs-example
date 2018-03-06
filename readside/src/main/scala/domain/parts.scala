package domain

object parts {
  sealed trait PartStatus extends Product with Serializable

  case object Damaged extends PartStatus

  case object Repaired extends PartStatus

  object PartStatus {
    def apply(name: String): PartStatus = name match {
      case "DAMAGED"  => Damaged
      case "REPAIRED" => Repaired
    }

    def nameOf(status: PartStatus): String = status match {
      case Damaged  => "DAMAGED"
      case Repaired => "REPAIRED"
    }
  }

  case class CarPart(name: String, carPlate: String, status: PartStatus = Damaged)
}
