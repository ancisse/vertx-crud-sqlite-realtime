package se.kry.codetest.migrate;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import se.kry.codetest.DBConnector;

import java.time.Instant;


public class DBMigration {

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    DBConnector connector = new DBConnector(vertx);
    connector.query(
            "CREATE TABLE IF NOT EXISTS service (id INTEGER PRIMARY KEY,name VARCHAR(40) NOT NULL,url VARCHAR(128) NOT NULL,createdAt DATE NOT NULL, status VARCHAR(40) );").setHandler(done -> {
      if(done.succeeded()){
        System.out.println("Migration : completed db creation");
        connector.query("INSERT INTO service (name, url, createdAt, status) VALUES (?, ?, ?, ?)",
                new JsonArray().add("kry").add("https://www.kry.se").add(Instant.now().toEpochMilli()).add("UNKNOW")).setHandler(insert -> {
          if (insert.succeeded()){
            System.out.println("Migration : completed db insert");
          }else {
            insert.cause().printStackTrace();
          }
        });
      } else {
        done.cause().printStackTrace();
      }
      vertx.close(shutdown -> {
        System.exit(0);
      });
    });



  }


}
