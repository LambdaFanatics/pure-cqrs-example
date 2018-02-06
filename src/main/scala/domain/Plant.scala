package domain


case class Plant(id: PlantId, name: String, country: String)

case class PlantId(value: String) extends AnyVal

object PlantId {
  implicit def stringToPlantId(value: String): PlantId = PlantId(value)
}
