import scala.concurrent.{ExecutionContextExecutor, Future}
import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.util.Timeout
import routes.RestApi
import com.typesafe.config.{Config, ConfigFactory}


object ServiceMain extends App with RequestTimeout {

  val config = ConfigFactory.load()
  val host = "0.0.0.0"
  val port = 9090

  implicit val system: ActorSystem = ActorSystem()
  implicit val executor: ExecutionContextExecutor = system.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val api = new RestApi(system, requestTimeout(config)).routes

  val bindingFuture: Future[ServerBinding] = Http().bindAndHandle(api, host, port)

  val log = Logging(system.eventStream, "main")

  try {
    bindingFuture.map { serverBinding =>
      log.info(s"Rest Api bound to ${serverBinding.localAddress}")
    }
  } catch {
      case ex: Exception =>
        log.error(ex, "Failed to bind to {}:{}", host, port)
        system.terminate()
    }
  }


trait RequestTimeout {
  import scala.concurrent.duration._

  def requestTimeout(config: Config): Timeout = {
    val t = config.getString("akka.http.server.request-timeout")
    val d = Duration(t)
    FiniteDuration(d.length, d.unit)
  }

}