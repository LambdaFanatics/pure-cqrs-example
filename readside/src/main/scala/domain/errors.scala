package domain

object errors {
  trait StoreError extends Product with Serializable
  case object UnexpectedStoreState extends StoreError
  case object GeneralStoreError extends StoreError
}
