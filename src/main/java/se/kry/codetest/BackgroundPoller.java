package se.kry.codetest;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;


public class BackgroundPoller  {
  private CrudRepository crudService;
  private  Vertx vertx;
  private  Router router;
  private ServiceHandler serviceHandler;

  public BackgroundPoller(CrudRepository crudService,ServiceHandler serviceHandler, Vertx vertx, Router router) {
    this.crudService = crudService;
    this.vertx = vertx;
    this.router = router;
    this.serviceHandler = serviceHandler;
  }

  public void pollServices() {

    crudService.getAll().setHandler(results -> {
      if (results.failed()) {
      } else {
        results.result().getRows().forEach(entries -> {
          WebClient.create(vertx).getAbs(entries.getString("url")).timeout(3000).send(response->{
            crudService.updateStatus(entries , response.succeeded() ? "OK" : "FAIL");
          });
        });

      }
    });

    router.get("/service").handler(serviceHandler::handleGetServices);
  }

  private void handleGetServices(RoutingContext routingContext) {
    crudService.getAll().setHandler(results -> {
      if (results.failed()) {
      } else {
        JsonArray arr = new JsonArray();
        results.result().getRows().forEach(arr::add);
        routingContext.response().putHeader("content-type", "application/json").end(arr.encode());
      }
    });
  }

}
