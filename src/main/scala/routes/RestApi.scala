package routes

import akka.actor.{ActorRef, ActorSystem}
import akka.util.Timeout
import akka.pattern.ask
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import messages.ProductMessage._
import messages._
import database.Database

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}
import StatusCodes._
import messages.DatabaseMessage.GetAllProducts
import play.api.libs.json.JsValue

class RestApi(system: ActorSystem, timeout: Timeout) extends RestRoutes {
  implicit val requestTimeout: Timeout = timeout
  implicit def executionContext: ExecutionContextExecutor = system.dispatcher

  def createProductActor(): ActorRef = system.actorOf(ProductMessage.props)
  def createDatabaseActor(): ActorRef = system.actorOf(DatabaseMessage.props)
}

trait RestRoutes extends ProductApi with ProductMarshaller {
  val service = "shop"

  protected val createProductRoute: Route = {
    pathPrefix(service / "products" / Segment) { product =>
      post {
        pathEndOrSingleSlash {
          entity(as[ProductDescription]) { ed =>
            onSuccess(createProduct(product, ed.quantity)) {
              case ProductCreated(product) => complete(Created, product)

              case ProductExists => val err = Error(s"$product product already exists!")
                complete(BadRequest, err)
            }
          }
        }
      }
    }
  }

  protected val getAllProductsRoute: Route = {
    pathPrefix(service / "products") {
      get {
        pathEndOrSingleSlash {
          onSuccess(getProducts()) { products =>
            complete(OK, products)
          }
        }
      }
    }
  }

  protected val getProductRoute: Route = {
    pathPrefix(service / "products" / Segment) { product =>
      get {
        pathEndOrSingleSlash {
          onSuccess(getProduct(product)){
            _.fold(complete(NotFound))(e => complete(OK, e))
          }
        }
      }
    }
  }

  protected val deleteProductRoute: Route = {
    pathPrefix(service / "products" / Segment) { product =>
      delete {
        pathEndOrSingleSlash {
          onSuccess(deleteProduct(product)) {
            _.fold(complete(NotFound))(e => complete(OK, e))
          }
        }
      }
    }
  }

  protected val purchaseProductRoute: Route = {
    pathPrefix(service / "products" / Segment / "buy") { product =>
      post {
        pathEndOrSingleSlash {
          entity(as[ItemRequest]) { request =>
            onSuccess(requestItems(product, request.quantity)) { items =>
              if(items.entries.isEmpty) complete(NotFound)
              else complete(Created, items)
            }
          }
        }
      }
    }
  }

  val routes: Route = createProductRoute ~ getAllProductsRoute ~ getProductRoute ~ deleteProductRoute ~ purchaseProductRoute
}

trait ProductApi {

  def createProductActor(): ActorRef
  def createDatabaseActor(): ActorRef

  implicit def executionContext: ExecutionContext
  implicit def requestTimeout: Timeout

  lazy val productActor: ActorRef = createProductActor()
  lazy val databaseActor: ActorRef = createDatabaseActor()

  def createProduct(product: String, quantity: Int): Future[ProductResponse] = {
    productActor.ask(CreateProduct(product, quantity)).mapTo[ProductResponse]
  }

//  def getProducts(): Future[Products] = productActor.ask(GetProducts).mapTo[Products]
//  def getProducts(): Future[JsValue] = Database.getAllProducts
  def getProducts(): Future[JsValue] = databaseActor.ask(GetAllProducts).mapTo[JsValue]

  def getProduct(product: String): Future[Option[Product]] = productActor.ask(GetProduct(product)).mapTo[Option[Product]]

  def deleteProduct(product: String): Future[Option[Product]] = productActor.ask(DeleteProduct(product)).mapTo[Option[Product]]

  def requestItems(product: String, quantity: Int): Future[SellerMessage.Items] = {
    productActor.ask(ProductMessage.GetQuantity(product, quantity)).mapTo[SellerMessage.Items]
  }
}
