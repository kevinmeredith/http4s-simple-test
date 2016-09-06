package net.repository

import net.model.PersonAST._
import scalaz._
import Scalaz._
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
    "org.postgresql.Driver", "jdbc:postgresql:myapp", "postgres", "postgres"
  )

  override def get(ssn: SSN): Task[FailedGetPerson \/ Option[Person]] = {
    getHelper(ssn) >>= {
      case \/-( (digits, name, age) :: Nil ) => Task.now { handleDbLookup( Person(digits, name, age) ).map(Some(_)) }
      case \/-(_ :: _)                       => Task.now { -\/(FoundMoreThanOnePerson(ssn)) }
      case \/-( Nil )                        => Task.now { \/-(None) }
      case -\/(failure)                      => Task.now { -\/(failure) }
    }
  }

  private def getHelper(ssn: SSN): Task[FailedGetPerson \/ List[(Int, String, Int)]] = {
    val result: Task[List[(Int, String, Int)]] =
      sql"SELECT ssn, name, age FROM person".query[(Int, String, Int)].list.transact(xa)
    result.map(\/-(_)).handle {
      case t => -\/(FailedGetPersonDbError(t))
    }
  }

  private def handleDbLookup(result: PersonError \/ Person): FailedGetPerson \/ Person = result match {
    case \/-(person)      => \/-(person)
    case -\/(personError) => -\/(FoundInvalidPerson(personError))
  }

}

trait PersonRepository {
  import PersonRepository._

  def get(ssn: SSN): Task[FailedGetPerson \/ Option[Person]]
}
