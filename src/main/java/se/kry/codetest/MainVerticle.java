package se.kry.codetest;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.bridge.BridgeEventType;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.ErrorHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;

import java.util.HashMap;



public class MainVerticle extends AbstractVerticle {
  private static final Logger logger = LoggerFactory.getLogger(MainVerticle.class);
  private HashMap<String, String> services = new HashMap<>();
  private DBConnector connector;
  private BackgroundPoller poller;
  private CrudRepository crudService;
  private ServiceHandler handler;

  @Override
  public void start(Future<Void> startFuture) {


    Router router = Router.router(vertx);

    connector = new DBConnector(vertx);
    crudService = new CrudRepository(connector);
    handler= new ServiceHandler(crudService);


    router.route("/eventbus/*").handler(eventBusHandler());
    router.mountSubRouter("/api", serviceApiRouter());
    router.route().failureHandler(errorHandler());
    router.route().handler(staticHandler());
    poller = new BackgroundPoller( crudService,handler, vertx,  router);
    vertx.setPeriodic(1000 * 60, timerId -> poller.pollServices());

    vertx
        .createHttpServer()
        .requestHandler(router)
        .listen(8080, result -> {
          if (result.succeeded()) {
            System.out.println("KRY code test service started");
            startFuture.complete();
          } else {
            startFuture.fail(result.cause());
          }
        });
  }

  private Router serviceApiRouter() {

    // Create a router object.
    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());

    router.route().consumes("application/json");
    router.route().produces("application/json");


    router.route("/service/:id").handler(handler::validateId);
    router.get("/service").handler(handler::handleGetServices);
    router.post("/service").handler(handler::handleAddService);
    router.put("/service/:id").handler(handler:: updateServiceName);
    router.delete("/service/:id").handler(handler:: delete);


    return router;
  }

  private SockJSHandler eventBusHandler() {
    BridgeOptions options = new BridgeOptions()
            .addOutboundPermitted(new PermittedOptions().setAddressRegex("service\\.[0-9]+"));
    return SockJSHandler.create(vertx).bridge(options, event -> {
      if (event.type() == BridgeEventType.SOCKET_CREATED) {
        logger.info("A socket was created");
      }
      event.complete(true);
    });
  }

  private ErrorHandler errorHandler() {
    return ErrorHandler.create(true);
  }

  private StaticHandler staticHandler() {
    return StaticHandler.create()
            .setCachingEnabled(false);
  }
}



