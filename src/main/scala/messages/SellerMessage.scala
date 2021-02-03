package messages

import actors.SellerActor
import akka.actor.Props

object SellerMessage {

  def props(name: String) = Props(new SellerActor(name))

  case class Add(items: Vector[Item])
  case class Buy(items: Int)
  case class Item(id: Int)
  case class Items(name: String, entries: Vector[Item] = Vector.empty[Item])

  case object GetQuantity
  case object Cancel
}
