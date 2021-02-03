package actors

import akka.actor.{Actor, PoisonPill}
import messages.ProductMessage

class SellerActor(name: String) extends Actor{
  import messages.SellerMessage._

  var items = Vector.empty[Item]

  def receive: PartialFunction[Any, Unit] = {
      case Add(newItems) => items = items ++ newItems

      case Buy(quantity) =>
        val entries = items.take(quantity)
        if (entries.size >= quantity) {
          sender() ! Items(name, entries)
          items = items.drop(quantity)
        } else sender() ! Items(name)

      case GetQuantity => sender() ! Some(ProductMessage.Product(name, items.size))
      case Cancel => sender() ! Some(ProductMessage.Product(name, items.size))
        self ! PoisonPill

  }

}
