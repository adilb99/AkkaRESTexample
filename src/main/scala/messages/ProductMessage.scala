package messages

import actors.ProductActor
import akka.actor.Props
import akka.util.Timeout

object ProductMessage {

  def props(implicit timeout: Timeout) = Props(new ProductActor)

  case class CreateProduct(name: String, quantity: Int) // create new product
  case class GetProduct(name: String) // get a product
  case object GetProducts // get all available products
  case class GetQuantity(name: String, requested: Int)

//  case class EditProduct(id: Int, name: String, desc: String)

  case class DeleteProduct(name: String)

  case class Product(name: String, quantity: Int)
  case class Products(products: Vector[Product])

  sealed trait ProductResponse
  case class ProductCreated(product: Product) extends  ProductResponse
  case object ProductExists extends ProductResponse

}
