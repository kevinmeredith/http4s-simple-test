package net.model

import shapeless._
import ops.nat._
import nat._
import syntax.sized._
import scalaz._
import Scalaz._
import std.AllInstances
import cats.data.Xor
import io.circe.{Decoder, DecodingFailure}
import net.model.PersonAST.InvalidSSNDigit

object PersonAST {


  sealed trait PersonError
  case class EmptyName(value: String)     extends PersonError
  case class InvalidAge(x: Int)           extends PersonError
  case class InvalidSSN(value: List[Int]) extends PersonError
  case class NegativeSSN(value: Int)      extends PersonError
  case class InvalidSSNDigit(value: Int)  extends PersonError

  // note - I made up this number
  private val GuinessBookWorldRecordsOldestHumanAge = 150
  private val MinAge                                = 0

  private val ValidSSNDigits: Set[Int] = (0 to 9).toSet

  class SSNDigit(value: Int)
  object SSNDigit {
    def apply(value: Int): PersonError \/ SSNDigit =
      if(ValidSSNDigits.contains(value)) \/-( new SSNDigit(value) ) else -\/( InvalidSSNDigit(value) )
  }

  // A USA Social Security Number has exactly 9 digits
  case class SSN(value: Sized[List[SSNDigit], _9])
  object SSN {

    def apply(value: List[Int]): PersonError \/ SSN = {
      val ssnDigits: List[PersonError \/ SSNDigit] =
        value.map(SSNDigit(_))
      val allOrNothing: PersonError \/ List[SSNDigit] =
        ssnDigits.sequenceU
      allOrNothing.flatMap { digits =>
        digits.sized[_9] match {
          case Some(xs) => \/-(SSN(xs))
          case None     => -\/(InvalidSSN(value))
        }
      }
    }


    // credit: Travis Brown in http://stackoverflow.com/a/39183581/409976
    implicit def decodeSized[L <: Nat, A <: Nat](implicit
      dl: Decoder[List[A]],
      A: LTEq[A, _9],
      ti: ToInt[L]
    ): Decoder[Sized[List[A], L]] = Decoder.instance { c =>
      dl(c).flatMap {as =>
        Xor.fromOption(as.sized[L], DecodingFailure(s"Sized[List[A], _${ti()}]", c.history))
      }
    }
  }

  class Name private (val value: String)
  object Name {
    def apply(x: String): PersonError \/ Name =
      if(x.trim.isEmpty) -\/(EmptyName(x)) else \/-(new Name(x))
  }

  class Age(val x: Int)
  object Age {
    def apply(x: Int): PersonError \/ Age =
      if(x >= MinAge && x <= GuinessBookWorldRecordsOldestHumanAge) \/-(new Age(x)) else -\/(InvalidAge(x))
  }

  class Person(ssn: SSN, name: Name, age: Age)
  object Person {
    def apply(ssn: Int, name: String, age: Int): PersonError \/ Person = for {
      digits    <- digitsHelper(ssn)
      validSSN  <- SSN(digits)
      validName <- Name(name)
      validAge  <- Age(age)
    } yield new Person(validSSN, validName, validAge)

    private def digitsHelper(ssn: Int): PersonError \/ List[Int] = {
      if(ssn == 0)       \/-(List.empty)
      else if (ssn < 0)  -\/(NegativeSSN(ssn))
      else {
        val digit     = ssn % 10
        val nextInput = ssn / 10
        digitsHelper(nextInput).map(xs => digit :: xs)
      }
    }

  }
}
