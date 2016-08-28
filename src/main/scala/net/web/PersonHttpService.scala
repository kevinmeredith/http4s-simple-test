package net.web

import org.http4s.HttpService
import org.http4s.dsl._
import net.service.PersonService
import shapeless._, nat._
import scalaz._, Scalaz._

object PersonHttpService {

  val personService = HttpService {
    case GET -> Root / "person" / IntVar(ssn) =>
      ???
//      PersonService.get(ssn) map {
//        case \/-(Some(person)) => Ok(List(person))
//        case \/-(None)         => Ok(List.empty)
//        case -\/(error)        => InternalServerError("Error occurred. Please contact System Administrator.")
//      }
  }


}
