package interpreter.memory

import cats.data.State
import cats.effect.Effect
import cats.implicits._
import domain._
import domain.validations._


/**
  * In memory validator
  *
  * The implementation is inspired by https://gist.github.com/channingwalton/2846428
  *
  * @tparam F
  */
class ValidationInMemoryInterpreter[F[_] : Effect] extends ValidationAlgebra[F] {

  // Internal interpreter model
  private type Store = Map[String, Car]

  private case class Part(name: String, damaged: Boolean)

  private case class Car(regPlate: String, parts: List[Part])

  /**
    * The internal state is just a map of the cars hashed by regPlate
    * Note: this is a var of an immutable data structure, updating directly this var is not thread safe.
    * So we need a concurrent construct like mutex, semaphore, mvar to secure the access of the resource.
    */
  private var internalState: Store = Map.empty


  // TODO clean up code
  // TODO use state combinators to remove the ugly if then else ...
  private def registerCarValidation(regPlate: String) = State[Store, Either[ValidationError, Unit]] { s =>
    if (s.contains(regPlate))
      (s, CarAlreadyRegistered.asLeft)
    else
      (s + (regPlate -> Car(regPlate, List())), ().asRight)
  }

  private def repairCarValidation(regPlate: String) = State[Store, Either[ValidationError, Unit]] { s =>
    if (!s.contains(regPlate))
      (s, CarNotRegistered.asLeft)
    else
      (s.updated(regPlate, s(regPlate).copy(parts = Nil)), ().asRight)
  }

  private def markPartValidation(regPlate: String, part: String) = State[Store, Either[ValidationError, Unit]] { s =>
    if (!s.contains(regPlate))
      (s, CarNotRegistered.asLeft)
    else if (s(regPlate).parts.exists(_.name == part))
      (s, PartIsAlreadyMarked.asLeft)
    else
    // TODO propably need to use something like optics in order to clean up the mess, for now keep it like this
      (s.updated(regPlate, s(regPlate).copy(parts = Part(part, damaged = true) :: s(regPlate).parts)), ().asRight)

  }

  private def repairPartValidation(regPlate: String, part: String) = State[Store, Either[ValidationError, Unit]] { s =>
    if (!s.contains(regPlate))
      (s, CarNotRegistered.asLeft)
    else if (!s(regPlate).parts.exists(_.name == part))
      (s, PartIsNotMarked.asLeft)
    else if (s(regPlate).parts.find(_.name == part).get.damaged) { // TODO REMOVE OUCH !!!!
      (s, PartIsNotDamaged.asLeft)
    } else {
      (s.updated(regPlate, s(regPlate).copy(parts = s(regPlate).parts.filter(_.name == part))), ().asRight)
    }

  }

  private def unmarkPartValidation(regPlate: String, part: String) = State[Store, Either[ValidationError, Unit]] { s =>
    if (!s.contains(regPlate))
      (s, CarNotRegistered.asLeft)
    else if (!s(regPlate).parts.exists(_.name == part))
      (s, PartIsNotMarked.asLeft)
    else {
      (s.updated(regPlate, s(regPlate).copy(parts = s(regPlate).parts.filter(_.name == part))), ().asRight)
    }

  }

  // FIXME handle concurrency issues (make thread safe)
  private def exec(state: State[Store, Either[ValidationError, Unit]]): F[Either[ValidationError, Unit]] = Effect[F].delay {
    val (newS, res) = state.run(internalState).value
    internalState = newS // TODO race condition without concurrency guard bad bad bad!!!
    res
  }

  /**
    * Attempt to register a car.
    * The operation is tested against the validator state.
    * Note: that on success the internal state of the validator is updated.
    *
    * @param regPlate registration plate of the car to register.
    * @return An Either[CarAlreadyRegistered.type, Unit] indicating an invalid operation or success
    */
  def attemptToRegisterCar(regPlate: String): F[Either[ValidationError, Unit]] =
    exec(registerCarValidation(regPlate))

  def attemptToRepairCar(regPlate: String): F[Either[ValidationError, Unit]] =
    exec(repairCarValidation(regPlate))

  def attemptToMarkPart(regPlate: String, part: String): F[Either[ValidationError, Unit]] =
    exec(markPartValidation(regPlate, part))

  def attemptToUnmarkPart(regPlate: String, part: String): F[Either[ValidationError, Unit]] =
    exec(unmarkPartValidation(regPlate, part))

  def attemptToRepairPart(regPlate: String, part: String): F[Either[ValidationError, Unit]] =
    exec(repairPartValidation(regPlate, part))
}

object ValidationInMemoryInterpreter {
  def apply[F[_] : Effect] = new ValidationInMemoryInterpreter[F]
}