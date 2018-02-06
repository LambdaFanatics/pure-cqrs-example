package domain


case class Plant(id: PlantId, name: String, country: String)

case class PlantId(value: Long) extends AnyVal

object PlantId {
  implicit def longToPlantId(value: Long): PlantId = PlantId(value)
}
