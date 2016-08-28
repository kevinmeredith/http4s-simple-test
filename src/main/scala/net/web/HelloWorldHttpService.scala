package net.web

import org.http4s._
import org.http4s.dsl._

object HelloWorldHttpService {

  val helloWorldService = HttpService {
    case GET -> Root / "hello" / name =>
      Ok(s"Hello, $name.")
  }

}
