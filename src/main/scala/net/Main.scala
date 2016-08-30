package net

import org.http4s.server.{Server, ServerApp}
import org.http4s.server.blaze.BlazeBuilder
import net.web.HelloWorldHttpService.helloWorldService
import net.web.PersonHttpService.personService
import doobie.imports._
import scalaz.concurrent.Task

object Main extends ServerApp {

  val xa = DriverManagerTransactor[Task](
    "org.postgresql.Driver", "jdbc:postgresql:test", "postgres", "postgres"
  )

  override def server(args: List[String]): Task[Server] = {
    BlazeBuilder
      .bindHttp(8080, "localhost")
      .mountService(helloWorldService, "/api")
      .mountService(personService,     "/api")
      .start
  }
}
