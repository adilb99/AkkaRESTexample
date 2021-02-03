package actors

import akka.actor._
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import database.Database
import messages.ProductMessage._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


class ProductActor(implicit timeout: Timeout) extends Actor{
  import messages.SellerMessage

  def createSeller(name: String): ActorRef = {
    context.actorOf(SellerMessage.props(name), name)
  }

  def receive: PartialFunction[Any, Unit] = {
      case CreateProduct(name, quantity) =>
        def create(): Unit = {
          val sellingItems = createSeller(name)

          val newItems = (1 to quantity).map {itemID => SellerMessage.Item(itemID)}.toVector

          sellingItems ! SellerMessage.Add(newItems)

          sender() ! ProductCreated(Product(name, quantity))

        }

        context.child(name).fold(create())(_ => sender() ! ProductExists)

      case GetQuantity(name, requested) =>
        def notFound(): Unit = sender() ! SellerMessage.Items(name)
        def buy(child: ActorRef): Unit = {
          child.forward(SellerMessage.Buy(requested))
        }

        context.child(name).fold(notFound())(buy)


      case GetProduct(name) =>
        def notFound() = sender() ! None
        def getProduct(child: ActorRef) = child.forward(SellerMessage.GetQuantity)
        context.child(name).fold(notFound())(getProduct)

      case GetProducts =>
        def getProducts = {
          context.children.map { child =>
            self.ask(GetProduct(child.path.name)).mapTo[Option[Product]]

          }



        }
        def convertToProducts(f: Future[Iterable[Option[Product]]]): Future[Products] = {
          f.map(_.flatten).map(l => Products(l.toVector))
        }
        pipe(convertToProducts(Future.sequence(getProducts))) to sender()



      case DeleteProduct(name) =>
        def notFound(): Unit = sender() ! None
        def deleteProduct(child: ActorRef): Unit = child forward SellerMessage.Cancel
        context.child(name).fold(notFound())(deleteProduct)


  }




}
