package net.model

import shapeless._
import ops.nat._
import nat._
import syntax.sized._
import scalaz._
import Scalaz._
import cats.data.Xor
import io.circe.{Decoder, DecodingFailure}
import argonaut._
import Argonaut._
import org.http4s.EntityEncoder
import scodec.bits.ByteVector

object PersonAST {

  sealed trait PersonError
  case class EmptyName(value: String)     extends PersonError
  case class InvalidAge(x: Int)           extends PersonError
  case class InvalidSSN(value: Int)       extends PersonError
  case class NegativeSSN(value: Int)      extends PersonError
  case class InvalidSSNDigit(value: Int)  extends PersonError

  // note - I made up this number
  private val GuinessBookWorldRecordsOldestHumanAge = 150
  private val MinAge                                = 0

  private val ValidSSNDigits: Set[Int] = (0 to 9).toSet

  class SSNDigit(val value: Int)
  object SSNDigit {
    def apply(value: Int): PersonError \/ SSNDigit =
      if(ValidSSNDigits.contains(value)) \/-( new SSNDigit(value) ) else -\/( InvalidSSNDigit(value) )
  }

  // A USA Social Security Number has exactly 9 digits
  case class SSN private (value: Sized[List[SSNDigit], _9])
  object SSN {

    def apply(value: List[Int], original: Int): PersonError \/ SSN = {
      val ssnDigits: List[PersonError \/ SSNDigit] =
        value.map(SSNDigit(_))
      val allOrNothing: PersonError \/ List[SSNDigit] =
        ssnDigits.sequenceU
      allOrNothing.flatMap { digits =>
        digits.sized[_9] match {
          case Some(xs) => \/-(SSN(xs))
          case None     => -\/(InvalidSSN(original))
        }
      }
    }

    def apply(ssn: Int): PersonError \/ SSN =
      digitsHelper(ssn) >>= {list => apply(list, ssn)}

    def digitsHelper(ssn: Int): PersonError \/ List[Int] = {
      if(ssn == 0)       \/-(List.empty)
      else if (ssn < 0)  -\/(NegativeSSN(ssn))
      else {
        val digit     = ssn % 10
        val nextInput = ssn / 10
        digitsHelper(nextInput).map(xs => xs ++ List(digit))
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

    def ssnToInt(ssn: SSN): BigDecimal = {
      val ssnDigits: List[SSNDigit] = ssn.value.unsized
      val singleDigits: List[Int]    = ssnDigits.map(_.value)
      singleDigits.reverse.zipWithIndex.foldRight(BigDecimal.valueOf(0)){ (elem, acc) =>
        val (num, index) = elem
        Math.pow(10, index.toDouble) * num + acc
      }
    }

  }

  class Name private (val value: String)
  object Name {
    def apply(x: String): PersonError \/ Name =
      if(x.trim.isEmpty) -\/(EmptyName(x)) else \/-(new Name(x))
  }

  class Age(val value: Int)
  object Age {
    def apply(x: Int): PersonError \/ Age =
      if(x >= MinAge && x <= GuinessBookWorldRecordsOldestHumanAge) \/-(new Age(x)) else -\/(InvalidAge(x))
  }

  class Person(val ssn: SSN, val name: Name, val age: Age)
  object Person {
    def apply(ssn: Int, name: String, age: Int): PersonError \/ Person = for {
      digits    <- SSN.digitsHelper(ssn)
      validSSN  <- SSN(digits, ssn)
      validName <- Name(name)
      validAge  <- Age(age)
    } yield new Person(validSSN, validName, validAge)


    implicit def PersonEncodeJson: EncodeJson[Person] =
      EncodeJson((p: Person) =>
        ("ssn" := SSN.ssnToInt(p.ssn)) ->: ("name" := p.name.value) ->: ("age" := p.age.value) ->: jEmptyObject
      )

    implicit val personEntityEncoder: EntityEncoder[Person] =
      EntityEncoder.simple[Person]()(person =>
        ByteVector( person.asJson.nospaces.getBytes )
      )
  }

}
