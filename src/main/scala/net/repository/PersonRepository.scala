package net.repository

import net.model.PersonAST._
import scalaz._
import Scalaz._
import std.AllInstances._
import scalaz.concurrent.Task
import doobie.imports._

object PersonRepository {
  sealed trait FailedGetPerson
  case class FailedGetPersonDbError(t: Throwable) extends FailedGetPerson
  case class FoundMoreThanOnePerson(ssn: SSN)     extends FailedGetPerson
  case class FoundInvalidPerson(e: PersonError)   extends FailedGetPerson
}

object PersonRepositoryImpl extends PersonRepository {

  import PersonRepository._

  val xa = DriverManagerTransactor[Task](
    "org.postgresql.Driver", "jdbc:postgresql:testing", "postgres", "postgres"
  )

  override def get(ssn: SSN): Task[FailedGetPerson \/ Option[Person]] = {
    val result: Task[List[(Int, String, Int)]] =
       sql"SELECT ssn, name, age FROM person".query[(Int, String, Int)].list.transact(xa)
    result >>= {
      case (digits, name, age) :: Nil => Task { getHelper( Person(digits, name, age) ).map(Some(_)) }
      case _ :: _                     => Task { -\/(FoundMoreThanOnePerson(ssn)) }
      case Nil                        => Task { \/-(None) }
    }

  }

  private def getHelper(result: PersonError \/ Person): FailedGetPerson \/ Person = result match {
    case \/-(person)      => \/-(person)
    case -\/(personError) => -\/(FoundInvalidPerson(personError))
  }

}

trait PersonRepository {
  import PersonRepository._

  def get(ssn: SSN): Task[FailedGetPerson \/ Option[Person]]
}
