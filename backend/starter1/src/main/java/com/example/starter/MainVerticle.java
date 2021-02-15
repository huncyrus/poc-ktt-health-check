package com.example.starter;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.VertxOptions;
import io.vertx.core.dns.AddressResolverOptions;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.Promise;
import io.vertx.core.MultiMap;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.core.logging.SLF4JLogDelegateFactory;
import io.vertx.ext.web.handler.CorsHandler;

import static io.vertx.core.Vertx.vertx;

/*
Reminder for me:

# Create db user!
sudo mysql
CREATE USER 'testuser'@'%' IDENTIFIED BY 'pass';
GRANT ALL PRIVILEGES ON *.* TO 'testuser'@'%';
SHOW GRANTS FOR 'testuser';
exit;

# Create db
sudo mysql -utestuser -ppass ktt_health_check < ktt.sql


# Build the project
Use terminal (or the IDE internal build, does not matter)
~./gradlew build

# Find the JAR file if the build succeed
You can found it in:
~./build/libs/<package-name>-fat.jar
Then you can run it :)

# Run project with java
~ java -jar ./build/libs/starter-1.0.0-SNAPSHOT-fat.jar -conf ./src/main/conf/app-config.json


~ cd fe
~ npm install
~ npm start
# http://localhost:3000
 */

/**
 * MainVerticle 
 * The backend project main core class.
 */
public class MainVerticle extends AbstractVerticle {
    private ServiceHandler sh_; // Member variable/cached service handler
    private SimpleModel model_; // Member variable/cached database layer

    @Override
    public void start() throws Exception {
        this.model_ = new SimpleModel(config(), vertx);
        this.sh_= new ServiceHandler(vertx);
        this.sh_.setDatabase(this.model_);

        // Routing
        // @todo move out routing from here later
        Router router = Router.router(vertx);
        router.route().handler(CorsHandler.create("*"));
        Route messageRoute = router.get("/api/message");

        router.get("/x").handler(z -> {
            System.out.println("[MV] is this works? ");
            z.response().end(Json.encode(new JsonObject().put("hello", "there")));
        });
        router.get("/api/all").handler(z -> {
            System.out.println("[MV] is this works? (/api/all ");

            model_
                .getAllUrlForUser(1)
                .onSuccess(dbResponse -> {
                    z
                        .response()
                        .putHeader("Access-Control-Allow-Origin", "*")
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .end(Json.encode(dbResponse))
                    ;

                })
                .onFailure(k -> {
                    z
                        .response()
                        .putHeader("Access-Control-Allow-Origin", "*")
                        .end("fail")
                    ;
                });
        });

        messageRoute.handler(data -> {
            data
                .response()
                .putHeader("Access-Control-Allow-Origin", "*")
                .end("Hello there general kenobi");
        });

        // @todo investigate this route why does not get the payload -,- 
        Route addRoute = router.post("/api/add");
        addRoute.handler(data -> {
            System.out.println("[MV][route /api/add] incoming data: " + data.getBodyAsString());
            try {
                JsonObject temp = data.getBodyAsJson();
                System.out.println("[MV][route /api/add] incoming payload: " + temp.toString());
                model_
                    .saveNew(1, temp.getString("server_address"), temp.getString("server_name"))
                    .onSuccess(z -> {
                        data
                            .response()
                            .putHeader("Access-Control-Allow-Origin", "*")
                            .end("ok")
                        ;
                    })
                    .onFailure(z -> {
                        data
                            .response()
                            .putHeader("Access-Control-Allow-Origin", "*")
                            .end("fail")
                        ;
                    })
                ;
            } catch (Exception err) {
                System.out.println("[MV][route][/api/add] exception: " + err.getMessage());

            }

        });


        Route getOneRoute = router.get("/api/get/:id");
        getOneRoute.handler(data -> {
            data
                .response()
                .putHeader("Access-Control-Allow-Origin", "*")
                .end("Hello there general kenobi")
            ;
        });
        Route removeRoute = router.delete("/api/:id/remove");
        removeRoute.handler(data -> {
            model_
                .remove((int) data.get(":id"), 1)
                .onSuccess(z -> {
                    data
                        .response()
                        .putHeader("Access-Control-Allow-Origin", "*")
                        .end("ok")
                    ;
                })
                .onFailure(z -> {
                    data
                        .response()
                        .putHeader("Access-Control-Allow-Origin", "*")
                        .end("fail")
                    ;
                });

            data
                .response()
                .putHeader("Access-Control-Allow-Origin", "*")
                .end("Hello there general kenobi")
            ;
        });
        Route updateRoute = router.post("/api/:id/update");
        updateRoute.handler(data -> {
            data
                .response()
                .putHeader("Access-Control-Allow-Origin", "*")
                .end("Hello there general kenobi")
            ;
        });

        /*
        // Attempt to bind manually a DNS resolve list...
        vertx(new VertxOptions().setAddressResolverOptions(
            new AddressResolverOptions()
            .addServer("192.168.0.1")
            .addServer("192.168.0.2:40000")
            .addServer("8.8.8.8")
            .addServer("4.4.4.4")
            .addServer("8.8.4.4")
        ));
        */

        // Create the HTTP server

        vertx.createHttpServer()
            // Handle every request using the router
            .requestHandler(router)
            // Start listening
            .listen(config().getInteger("server.port", 8088))
            // Print the port
            .onSuccess(server ->
                System.out.println(
                    "HTTP server started on port " + server.actualPort()
                )
            );

        this.sh_.run(); // Run service handler after everything else established

    }

    /**
     * Vertx call ~e.g.: destructor. Shall close the database connection.
     */
    public void stop() {
        this.model_.closeConnection();
    }
}
