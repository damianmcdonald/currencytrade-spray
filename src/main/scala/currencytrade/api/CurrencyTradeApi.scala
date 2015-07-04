/*
 * Copyright 2015 Damian McDonald
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package currencytrade.api

import java.util.concurrent.Executors

import currencytrade.data.{ MongoDBDataAccess, DataAccess }
import currencytrade.mock.ClientMockService
import currencytrade.trade.{ TradeRetrieveService, TradePersistService }
import currencytrade.websocket.WebSocketServer
import akka.actor.{ ActorSystem, Props }
import akka.event.Logging.InfoLevel
import scala.concurrent.ExecutionContext
import spray.http.{ HttpRequest, StatusCodes }
import spray.routing.directives.LogEntry
import spray.routing.Directives
import spray.routing._

/**
 * Trait to mixin an implicit [[akka.actor.ActorSystem]]
 *
 * This trait is based on the SprayEasterEggs project.
 * Please view this project for more details.
 *
 * @see https://github.com/cuali/SprayEasterEggs
 */
trait AbstractSystem {
  implicit def system: ActorSystem
}

/**
 * Associates each [[akka.actor.Actor]] with their corresponding route handler service
 *
 * This trait is based on the SprayEasterEggs project.
 * Please view this project for more details.
 *
 * @see https://github.com/cuali/SprayEasterEggs
 */
trait CurrencyTradeApi extends RouteConcatenation with StaticRoute with AbstractSystem {
  this: MainActors =>
  private def showReq(req: HttpRequest) = LogEntry(req.uri, InfoLevel)

  val rootService = system.actorOf(Props(new RootService[BasicRouteActor](routes)), "routes")
  lazy val routes = logRequest(showReq _) {
    new TradePersistService(dataAccess).route ~
      new TradeRetrieveService(notifier).route ~
      new ClientMockService().route ~
      staticRoute
  }
  val wsService = system.actorOf(Props(new RootService[WebSocketServer](wsroutes)), "wss")
  lazy val wsroutes = logRequest(showReq _) {
    new TradeRetrieveService(notifier).wsroute ~
      complete(StatusCodes.NotFound)
  }
}

/**
 * Static route definitions
 *
 * This trait is based on the SprayEasterEggs project.
 * Please view this project for more details.
 *
 * @see https://github.com/cuali/SprayEasterEggs
 */
trait StaticRoute extends Directives {
  this: AbstractSystem =>

  lazy val staticRoute =
    pathPrefix("scripts") {
      getFromResourceDirectory("web/scripts/")
    } ~
      pathPrefix("css") {
        getFromResourceDirectory("web/css/")
      } ~
      pathPrefix("images") {
        getFromResourceDirectory("web/images/")
      } ~
      pathPrefix("fonts") {
        getFromResourceDirectory("web/fonts/")
      } ~
      pathPrefix("api") {
        getFromResourceDirectory("web/api/")
      } ~
      pathEndOrSingleSlash {
        getFromResource("web/index.html")
      } ~ complete(StatusCodes.NotFound)
}

/**
 * Mixes in data access, execution context for Futures and custom rejection handlers
 */
trait CoreAdditions extends Directives {

  /** The data access reference */
  lazy val dataAccess: DataAccess = new MongoDBDataAccess

  /** The Execution Context for blocking operations */
  implicit val blockingExecutionContext = {
    import currencytrade.Configuration._
    ExecutionContext.fromExecutor(Executors.newFixedThreadPool(threadPool))
  }

  /** The Custom Rejection Handler for API route requests */
  implicit val customRejectionHandler = RejectionHandler {
    case MethodRejection(supported) :: _ => {
      complete(StatusCodes.MethodNotAllowed, "Unsupported Http method.")
    }
    case UnsupportedRequestContentTypeRejection(errorMsg) :: _ => complete(StatusCodes.UnsupportedMediaType, "Unsupported content type. " + errorMsg)
  }
}
