package net.service

import net.model.PersonAST.{Person, SSN, SSNDigit}
import shapeless._
import nat._
import shapeless.ops.nat.LTEq

import scalaz._
import Scalaz._
import scalaz.concurrent.Task

object PersonService {

  sealed trait FailedGetPerson
  case class FailedGetPersonDbError(t: Throwable) extends FailedGetPerson
  case class FoundMoreThanOnePerson(ssn: Nat)     extends FailedGetPerson

  def get[A <: Nat](ssn: SSNDigit)(implicit ev: LTEq[A, _9]): Task[FailedGetPerson \/ Option[Person[A]]] =
    ???


}
