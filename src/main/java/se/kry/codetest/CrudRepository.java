package se.kry.codetest;

import io.vertx.core.Future;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.web.RoutingContext;

import java.time.Instant;
import java.util.NoSuchElementException;

public class CrudRepository {

    private DBConnector connector;


    public CrudRepository(DBConnector connector) {
        this.connector = connector;
    }


    public Future<ResultSet> addOne(JsonObject service){


       return connector.query("INSERT INTO service (name, url, createdAt, status) VALUES (?, ?, ?, ?)",
                new JsonArray()
                        .add(service.getString("name"))
                        .add(service.getString("url"))
                        .add(Instant.now().toEpochMilli())
                        .add("UNKNOWN"));
    }

    public  Future<ResultSet> getAll() {
        return connector.query("SELECT * FROM service");
    }

    public Future<ResultSet> updateName( Long id,JsonObject service) {

        return connector.query("UPDATE service SET name = ? where id = ? ", new JsonArray().add(service.getString("name")).add(id));
    }

    public Future<ResultSet> updateStatus(JsonObject service, String newStatus) {

        return connector.query("UPDATE service SET status = ? where id = ? ", new JsonArray().add(newStatus).add(service.getLong("id")));
    }


    public Future<ResultSet>  delete(Long id) {

        return connector.query("DELETE FROM service WHERE id = ?",new JsonArray().add(id));
    }

}
