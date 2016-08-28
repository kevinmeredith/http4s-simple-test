package net.model

import shapeless._
import ops.nat._
import nat._
import syntax.sized._
import scalaz._
import Scalaz._
import cats.data.Xor
import io.circe.{ Decoder, DecodingFailure }

object PersonAST {

  case object EmptyName
  case class InvalidAge(x: Int)

  // note - I made up this number
  private val GuinessBookWorldRecordsOldestHumanAge = 150
  private val MinAge                                = 0

  // case class SSNDigit[N <: Nat](n: N)(implicit ev: LTEq[N, _9])

  sealed trait SSNDigit {
    type N = Nat
    val n: N
    implicit val ev: LTEq[N, _9] = implicitly[LTEq[N, _9]]
  }
  case object Zero  extends SSNDigit   { override val n = _0 }
  case object One   extends SSNDigit   { override val n = _1 }
  case object Two   extends SSNDigit   { override val n = _2 }
  case object Three extends SSNDigit   { override val n = _3 }
  case object Four  extends SSNDigit   { override val n = _4 }
  case object Five  extends SSNDigit   { override val n = _5 }
  case object Six   extends SSNDigit   { override val n = _6 }
  case object Seven extends SSNDigit   { override val n = _7 }
  case object Eight extends SSNDigit   { override val n = _8 }
  case object Nine  extends SSNDigit   { override val n = _9 }


  // A USA Social Security Number has exactly 9 digits, each of which
  // may be 0 through 9.
  case class SSN(value: Sized[List[SSNDigit], _9])
  object SSN {
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
    def apply(x: String): \/[EmptyName.type, Name] =
      if(x.trim.isEmpty) -\/(EmptyName) else \/-(new Name(x))
  }

  class Age(val x: Int)
  object Age {
    def apply(x: Int): InvalidAge \/ Age =
      if(x >= MinAge && x <= GuinessBookWorldRecordsOldestHumanAge) \/-(new Age(x)) else -\/(InvalidAge(x))
  }

  case class Person[A <: Nat](ssn: SSN, name: Name, age: Age)(implicit ev: LTEq[A, _9])
}
