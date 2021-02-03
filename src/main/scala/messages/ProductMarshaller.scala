package messages

import play.api.libs.json._
import messages.ProductMessage._
import de.heikoseeberger.akkahttpplayjson._
import messages.SellerMessage._

case class ProductDescription(quantity: Int){
  require(quantity > 0)
}

case class ItemRequest(quantity: Int){
  require(quantity > 0)
}


case class Error(message: String)

trait ProductMarshaller extends PlayJsonSupport {

  implicit val productDescriptionFormat: OFormat[ProductDescription] = Json.format[ProductDescription]
  implicit val itemRequests: OFormat[ItemRequest] = Json.format[ItemRequest]
  implicit val errorFormat: OFormat[Error] = Json.format[Error]
  implicit val productFormat: OFormat[Product] = Json.format[Product]
  implicit val productsFormat: OFormat[Products] = Json.format[Products]
  implicit val itemFormat: OFormat[Item] = Json.format[Item]
  implicit val itemsFormat: OFormat[Items] = Json.format[Items]
}

object ProductMarshaller extends ProductMarshaller
