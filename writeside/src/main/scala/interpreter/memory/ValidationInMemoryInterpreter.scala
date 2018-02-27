package interpreter.memory

import cats.data.StateT
import cats.effect.Effect
import cats.implicits._
import domain._
import domain.validations.{CarNotDamaged, _}
import fs2.async.mutable.Semaphore

/**
  * In memory validator
  *
  * The implementation is inspired by https://gist.github.com/channingwalton/2846428
  *
  * @tparam F
  */
class ValidationInMemoryInterpreter[F[_] : Effect](semaphore: Semaphore[F]) extends ValidationAlgebra[F] {

  private object internal {

    // Internal interpreter model
    type Store = Map[String, Car]

    case class Part(name: String, damaged: Boolean)

    case class Car(regPlate: String, parts: List[Part])

    type Valid[A] = Either[ValidationError, A]

    /**
      * The internal state is just a map of the cars hashed by regPlate
      * Note: this is a var of an immutable data structure, updating directly this var is not thread safe.
      */
    var internalState: Store = Map.empty

    def checkCarNotRegistered(regPlate: String) = StateT[Valid, Store, Unit] { s =>
      s.get(regPlate).toLeft((s, ())).leftMap(_ => CarAlreadyRegistered)
    }

    def checkCarRegistered(regPlate: String) = StateT[Valid, Store, Car] { s =>
      s.get(regPlate).toRight(CarNotRegistered).map(car => (s, car))
    }

    def checkCarIsDamaged(regPlate: String) = StateT[Valid, Store, Car] { s =>
      s.get(regPlate).toRight(CarNotRegistered).flatMap ( car =>
        if (!car.parts.exists(_.damaged == true))
          CarNotDamaged.asLeft
        else
          (s, car).asRight
      )
    }

    def checkPartIsMarked(regPlate: String, part: String) = StateT[Valid, Store, Part] { s =>
      s.get(regPlate).toRight(CarNotRegistered).flatMap ( car =>
        car.parts.find(_.name == part).toRight(PartNotMarked).map((s,_))
      )
    }

    def checkPartIsNotMarked(regPlate: String, part: String) = StateT[Valid, Store, Unit] { s =>
      s.get(regPlate).toRight(CarNotRegistered).flatMap ( car =>
        car.parts.find(_.name == part).toLeft((s, ())).leftMap(_ => PartAlreadyMarked)
      )
    }

    def registerCar(regPlate: String) = StateT[Valid, Store, Car] { s =>
      val newCar = Car(regPlate, List())
      (s + (regPlate -> newCar), newCar).asRight[ValidationError]
    }

    def repairCar(c: Car) =  StateT[Valid, Store, Car] { s =>
      val repaired = c.copy( parts = c.parts.map(_.copy(damaged = false)))
      (s + (repaired.regPlate -> repaired), repaired).asRight[ValidationError]
    }

    def markPart(c: Car, partName: String) = StateT[Valid,Store, Part] { s =>
      val p = Part(partName, damaged = true)
      val updated = c.copy(parts = p :: c.parts)
      (s + (updated.regPlate -> updated), p).asRight[ValidationError]
    }

    def unmarkPart(c: Car, p: Part) = StateT[Valid,Store, Part] { s =>
      val updated = c.copy(parts = c.parts.filterNot(_.name == p.name))
      (s + (updated.regPlate -> updated), p).asRight[ValidationError]
    }

    def repairPart(c: Car, p: Part) = StateT[Valid,Store, Part] { s =>
      val updated = c.copy(parts = c.parts.map{ each =>
        if (each.name == p.name)
          p.copy(damaged = false)
        else each
      })
      (s + (updated.regPlate -> updated), p).asRight[ValidationError]
    }


    private def exec(state: StateT[Valid, Store, Unit]): F[Valid[Unit]] = Effect[F].delay (
      state.run(internalState).map { case (newState, _) =>
        internalState = newState
        ()
      }
    )


    def secureExec(state: StateT[Valid, Store, Unit]): F[Valid[Unit]] = for {
      s <- Effect[F].delay(semaphore)
      _ <- s.decrement
      res <- exec(state)
      _ <- s.increment
    } yield res
  }

  import internal._

  /**
    * Attempt to register a car.
    * The car is registered only if it is not already registered.
    * Note: that on success the internal state of the validator is updated.
    *
    * @param regPlate registration plate of the car.
    *
    * @return Either[ValidationError, Unit]
    */
  def attemptToRegisterCar(regPlate: String): F[Either[ValidationError, Unit]] = secureExec {
    for {
      _ <- checkCarNotRegistered(regPlate)
      _ <- registerCar(regPlate)
    } yield ()
  }

  def attemptToRepairCar(regPlate: String): F[Either[ValidationError, Unit]] =  secureExec {
    for {
      c <- checkCarRegistered(regPlate)
      c <- checkCarIsDamaged(c.regPlate)
      _ <- repairCar(c)
    } yield ()
  }

  def attemptToMarkPart(regPlate: String, part: String): F[Either[ValidationError, Unit]] = secureExec {
    for {
      c <- checkCarRegistered(regPlate)
      _ <- checkPartIsNotMarked(c.regPlate, part)
      _ <- markPart(c, part)
    } yield ()
  }

  def attemptToUnmarkPart(regPlate: String, part: String): F[Either[ValidationError, Unit]] = secureExec {
    for {
      c <- checkCarRegistered(regPlate)
      p <- checkPartIsMarked(regPlate, part)
      _ <- unmarkPart(c, p)
    } yield ()
  }

  def attemptToRepairPart(regPlate: String, part: String): F[Either[ValidationError, Unit]] = secureExec {
    for {
      c <- checkCarRegistered(regPlate)
      p <- checkPartIsMarked(c.regPlate, part)
      _ <- repairPart(c, p)
    } yield ()
  }
}

object ValidationInMemoryInterpreter {
  def apply[F[_] : Effect](semaphore: Semaphore[F]) = new ValidationInMemoryInterpreter[F](semaphore)
}