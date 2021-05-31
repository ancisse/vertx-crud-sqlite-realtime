package se.kry.codetest;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.web.RoutingContext;

public class ServiceHandler {

    public ServiceHandler(CrudRepository crudRepository) {
        this.crudRepository = crudRepository;
    }

    private final CrudRepository crudRepository;

    public void handleGetServices(RoutingContext routingContext) {

        Future<ResultSet> services = crudRepository.getAll();

        services.setHandler(results->{
            if (results.succeeded()){
                JsonArray arr = new JsonArray();
                results.result().getRows().forEach(arr::add);
                routingContext.response()
                        .putHeader("content-type", "application/json")
                        .setStatusCode(200)
                        .end(arr.encodePrettily());
            }else if (results.failed()){
                routingContext.response()
                        .putHeader("content-type", "application/json")
                        .setStatusCode(404)
                        .end();
            }

        });

    }


    public void handleAddService(RoutingContext routingContext){

        JsonObject service = routingContext.getBodyAsJson();
        crudRepository.addOne(routingContext.getBodyAsJson())
                .setHandler(result -> {
                    if (result.succeeded()){
                        routingContext.response()
                                .setStatusCode(201)
                                .putHeader("content-type", "application/json; charset=utf-8")
                                .end(service.encodePrettily());

                    }else if (result.failed()){
                        /*service.put(service.getString("url"),service.getString("name"));
                        response.end();*/
                    }
                });
    }

    public void updateServiceName(RoutingContext routingContext) {
        JsonObject service = routingContext.getBodyAsJson();
        Long id = Long.valueOf(routingContext.pathParam("id"));
        if (id == null || service == null) {
            routingContext.response().setStatusCode(400).end();
        }else {
            crudRepository.updateName(id,service).setHandler(result -> {
                if (result.succeeded()) {
                    routingContext.response().
                            putHeader("content-type", "application/json")
                            .setStatusCode(201)
                            .end(service.encodePrettily());
                } else if (result.failed()){

                }
            });
        }

    }

    public void delete(RoutingContext routingContext) {
        Long id = Long.valueOf(routingContext.pathParam("id"));
        crudRepository.delete(id).setHandler(result -> {
            if (result.succeeded()) {
                routingContext.response().setStatusCode(204).end();
            } else if (result.failed()){
               // routingContext.response().end();
            }
        });
    }


    public void validateId(RoutingContext routingContext) {
        try {
            routingContext.put("id", Long.parseLong(routingContext.pathParam("id")));
            // continue with the next handler in the route
            routingContext.next();
        } catch (NumberFormatException e) {
            //writeError(routingContext, e.getCause());
        }
    }
}
