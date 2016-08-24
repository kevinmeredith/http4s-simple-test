package net.model

import shapeless._
import nat._
import syntax.sized._
import scalaz._
import Scalaz._

object PersonAST {

  case class SSN(value: Sized[List[Int], _8])
  case class Name(value: String)

  case class Person(ssn: SSN, name: Name, age: Fin[_])

}
