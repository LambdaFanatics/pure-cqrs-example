import domain.{CarPartStoreAlgebra, CarStoreAlgebra}

trait StoreAlgebra[F[_]] extends CarStoreAlgebra[F] with CarPartStoreAlgebra[F]{

}
