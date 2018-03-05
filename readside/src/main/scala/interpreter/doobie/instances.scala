package interpreter.doobie

import domain.cars.DamageStatus
import doobie.util.meta.Meta

object instances {

  implicit val statusMeta: Meta[DamageStatus] =
    Meta[String].xmap(DamageStatus.apply, DamageStatus.nameOf)

}
