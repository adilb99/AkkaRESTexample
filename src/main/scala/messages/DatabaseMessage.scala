package messages

import actors.DatabaseActor
import akka.actor.Props

object DatabaseMessage {
  def props = Props(new DatabaseActor)

  case object GetAllProducts
  case class GetProductByID(id: Int)

  case class CreateNewProduct(id: Int, name: String, quantity: Int, description: String, price: Int)
}
