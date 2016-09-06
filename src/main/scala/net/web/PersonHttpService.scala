package net.web

import net.model.PersonAST._
import org.http4s._
import org.http4s.dsl._
import net.repository.PersonRepository
import net.repository.PersonRepository.FailedGetPerson
import scalaz.concurrent.Task
import scalaz._
import argonaut._

object PersonHttpService {

  sealed trait PersonServiceError
  case class InvalidRequest(e: PersonError) extends PersonServiceError
  case class RepoError(e: FailedGetPerson)  extends PersonServiceError

  def personService(personRepo: PersonRepository) = HttpService {
    case GET -> Root / "person" / IntVar(ssnInput) =>
      val result: Task[PersonServiceError \/ Option[Person]] = for {
        ssn    <- Task.now { validateSsn(ssnInput) }
        lookup <- getHelper(ssn, personRepo)
      } yield lookup
      result.flatMap {
        case \/-(Some(person))          => Ok(person)
        case \/-(None)                  => Ok(JsonObject.empty.toString)
        case -\/(InvalidRequest(error)) => BadRequest(s"Error: ${error.toString}.")
        case -\/(RepoError(error))      => InternalServerError(s"App failed due to ${error}.")
      }.handleWith {
        case t => InternalServerError ( t.getMessage )
      }
  }

  private def getHelper(ssn: PersonServiceError \/ SSN,
                        personRepo: PersonRepository): Task[PersonServiceError \/ Option[Person]] =
    ssn match {
      case \/-(validSSN) =>
        personRepo.get(validSSN) map {
          case \/-(result) => \/-(result)
          case -\/(error)  => -\/(RepoError(error))
        }
      case -\/(invalid) => Task.now { -\/(invalid) }
    }

  private def validateSsn(ssn: Int): PersonServiceError \/ SSN =
    SSN(ssn) match {
      case \/-(validSsn) => \/-(validSsn)
      case -\/(error)    => -\/(InvalidRequest(error))
    }


}
