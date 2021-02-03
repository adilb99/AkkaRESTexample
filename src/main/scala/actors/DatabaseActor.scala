package actors

import akka.actor._
import akka.pattern.{ask, pipe}
import database.Database
import messages.DatabaseMessage.GetAllProducts

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class DatabaseActor extends Actor{
  import messages.DatabaseMessage

  def receive: PartialFunction[Any, Unit] = {
    case GetAllProducts =>
      pipe(Database.getAllProducts) to sender()
  }
}
