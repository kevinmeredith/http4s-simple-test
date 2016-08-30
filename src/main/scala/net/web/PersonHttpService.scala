package net.web

import doobie.imports._
import org.http4s.HttpService
import org.http4s.dsl._
import net.service.PersonService
import shapeless._
import nat._

import scalaz._
import Scalaz._

object PersonHttpService {

  val personService = HttpService {
    case GET -> Root / "person" / IntVar(ssn) => ???
      //PersonService.get(???)
//      PersonService.get(ssn) map {
//        case \/-(Some(person)) => Ok(List(person))
//        case \/-(None)         => Ok(List.empty)
//        case -\/(error)        => InternalServerError("Error occurred. Please contact System Administrator.")
//      }
  }


}
