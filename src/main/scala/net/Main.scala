package net

import org.http4s.server.{Server, ServerApp}
import org.http4s.server.blaze.BlazeBuilder
import net.web.HelloWorldService.helloWorldService

import scalaz.concurrent.Task

object Main extends ServerApp {

  override def server(args: List[String]): Task[Server] = {
    BlazeBuilder
      .bindHttp(8080, "localhost")
      .mountService(helloWorldService, "/api")
      .start
  }
}
