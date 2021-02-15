package com.example.starter;
import io.vertx.core.*;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.mysqlclient.*;
import io.vertx.sqlclient.*;

/**
 * Simple model
 * Database layer, uses Vertx Mysql client.
 * 
 * @todo finish the future rewrite because first implementation was not correct (used future.otherwise instead of .complete)
 */
public class SimpleModel {
    private Pool mySqlPool_;
    private Vertx vertx_;
    private JsonObject config_;

    public SimpleModel(JsonObject config, Vertx vertx) {
        this.config_ = config;
        this.vertx_ = vertx;
        System.out.println("[SM][_] Simple model started here!");
        System.out.println("[SM][_] db info: " + config_.getInteger("mysql.port") + ", host: " + config_.getString("mysql.host"));
        this.connect();
        this.testConnection();
        this.mockAnUser();
        //this.closeConnection();
    }

    private void testConnection () {
        this.mySqlPool_.getConnection(ar -> {
            System.out.println("[SM][_] MySQL Connection present.");
            /*
            String sql = "SELECT * FROM ktt_health_check WHERE 1";
            ar.withTransaction(client -> client
                .query(sql)
                .execute()
                .onSuccess(v -> System.out.println("[SM][_] Transaction succeeded"))
                .onFailure(err -> System.out.println("[SM][_] Transaction failed: " + err.getMessage()))
            );
            */
        });
    }

    private void mockAnUser () {
        this
            .getAllUrlForUser(1)
            .onSuccess(x -> System.out.println("[SQ][Success] " + x))
            .onFailure(err -> System.out.println("[SQL][Error] " + err))
            .onComplete(ok -> System.out.println("[SQL][ok] done"))
        ;
    }

    public void connect() {
        MySQLConnectOptions connectOptions = new MySQLConnectOptions()
            .setPort(this.config_.getInteger("mysql.port", 3306))
            .setHost(this.config_.getString("mysql.host", "127.0.0.1"))
            .setDatabase(this.config_.getString("mysql.database", "ktt_health_check"))
            .setUser(this.config_.getString("mysql.user", "root"))
            .setPassword(this.config_.getString("mysql.pass", "root"))
            .setCharset("utf8");

        PoolOptions poolOptions = new PoolOptions()
            .setMaxSize(5);

        this.mySqlPool_ = Pool.pool(this.vertx_, connectOptions, poolOptions);
    }


    public void closeConnection () {
        this.mySqlPool_.close();
    }



    public Future<JsonArray> getAllUrlForUser(int userId) {
        Future<JsonArray> result = Future.future(x -> {
            String sql = "SELECT " +
                "id, service_url, service_name, cr_date, " +
                "mod_date, service_enabled, response_time, service_status " +
                "FROM " +
                "ktt_health_check " +
                "WHERE " +
                "cr_user = ? " +
                "ORDER BY cr_date DESC"
            ;

            this.mySqlPool_.withTransaction(client -> client
                .preparedQuery(sql)
                .execute(Tuple.of(userId))
                .onSuccess(v -> {
                    System.out.println("[SM][_] Transaction succeeded");
                    JsonArray test2 = new JsonArray();

                    String temp = v.iterator().next().getString("service_url");
                    System.out.println("[SQL] ez egy teszt kinyerés...: " + temp);

                    for (Row row : v) {
                        //System.out.println("[SQL] row = " + row.toJson());
                        test2.add(row.toJson());
                    }

                    x.complete(test2);
                })
                .onFailure(err -> {
                    System.out.println("[SM][_] Transaction failed: " + err.getMessage());
                    x.fail("Error: " + err.getMessage());
                })
            );
        });

        return result;
    }

    /**
     * Get all available (enabled) url from the database. This method shall be used ONLY by the service handler
     * itself.
     *
     * @return
     */
    public Future<JsonArray> getAllUrl () {
        Future<JsonArray> result = Future.future(x -> {
            String sql = "SELECT " +
                "id, service_url, service_name, cr_date, " +
                "mod_date, service_enabled, response_time, " +
                "service_status " +
                "FROM " +
                "ktt_health_check " +
                "WHERE " +
                " ? " +
                //"service_enabled = 1" +
                "ORDER BY cr_date DESC";

            this.mySqlPool_.withTransaction(client -> client
                .preparedQuery(sql)
                .execute(Tuple.of(1))
                .onSuccess(v -> {
                    System.out.println("[SM][gau] Transaction succeeded");
                    JsonArray test2 = new JsonArray();

                    String temp = v.iterator().next().getString("service_url");
                    System.out.println("[SQL][gau] ez egy teszt kinyerés...: " + temp);

                    for (Row row : v) {
                        System.out.println("[SQL][gau] row = " + row.toJson());
                        test2.add(row.toJson());
                    }

                    x.complete(test2);
                })
                .onFailure(err -> {
                    System.out.println("[SM][gau] Transaction failed: " + err.getMessage() + ", " + err.getCause() + ", " + err.getStackTrace());
                    JsonArray temp2 = new JsonArray();
                    x.fail("transaction failed");
                })
            );
        });

        return result;
    }

    public Future<Boolean> updateOneForScan (int id, boolean available, int response_time) {
        Future<Boolean> result = Future.future(x -> {
            String sql = "UPDATE " +
                "ktt_health_check " +
                "SET " +
                "mod_date = CURRENT_TIMESTAMP, service_status = ?, response_time = ? " +
                "WHERE " +
                "id = ? " +
                "LIMIT 1;"
            ;

            this.mySqlPool_.withTransaction(client -> client
                .preparedQuery(sql)
                .execute(Tuple.of(available, response_time, id))
                .onSuccess(v -> {
                    x.complete(true);
                })
                .onFailure(err -> {
                    x.fail("Error: " + err.getMessage());
                })
            );
        });

        return result;
    }

    public Future<Boolean> saveNew(int userId, String server_address, String server_name) {
        String sql = "INSERT INTO " +
            "ktt_health_check " +
            "(service_url, service_name, cr_user) " +
            "VALUES " +
            "(?, ?, ?);";

        Future<Boolean> result = Future.future(x -> {
            this.mySqlPool_.withTransaction(client -> client
                .preparedQuery(sql)
                .execute(Tuple.of(userId, server_address, server_name))
                .onSuccess(v -> {
                    System.out.println("[SM][sN] Transaction succeeded");
                    //result.otherwise(true).toCompletionStage();
                    x.complete(true);
                })
                .onFailure(err -> {
                    System.out.println("[SM][saveNew] error: " + err);
                    //result.otherwise(false).toCompletionStage();
                    x.fail("Error: " + err);
                })
            );
        });

        return result;
    }

    public Future<Boolean> updateExisting (int userId, int id, String server_address, String server_name, boolean enabled) {
        String sql = "UPDATE " +
            "ktt_health_check " +
            "SET " +
            "service_url = ?, service_name = ?, service_enabled = ? " +
            "WHERE " +
            "cr_user = ? AND id = ? " +
            "lIMIT 1";

        Future<Boolean> result = Future.failedFuture("false");
        this.mySqlPool_.withTransaction(client -> client
            .preparedQuery(sql)
            .execute(Tuple.of(server_address, server_name, enabled, userId, id))
            .onSuccess(v -> {
                System.out.println("[SM][UpdateExisting] Transaction succeeded");
                result.otherwise(true).toCompletionStage();
            })
            .onFailure(err -> {
                System.out.println("[SM][UpdateExisting] error: " + err);
                result.otherwise(false).toCompletionStage();
            })
        );

        return result;
    }

    public Future<Boolean> remove (int id, int userId) {
        Future<Boolean> result = Future.failedFuture("false");
        String sql = "" +
            "DELETE FROM " +
            "ktt_health_check " +
            "WHERE " +
            "id = ? AND cr_user = ? " +
            "LIMIT 1;";

        this.mySqlPool_.withTransaction(client -> client
            .preparedQuery(sql)
            .execute(Tuple.of(id, userId))
            .onSuccess(v -> {
                System.out.println("[SM][remove] Transaction succeeded");
                result.otherwise(true).toCompletionStage();
            })
            .onFailure(err -> {
                System.out.println("[SM][remove] error: " + err);
                result.otherwise(false).toCompletionStage();
            })
        );

        return result;
    }

    /**
     * Get a service ID and a UserID and determine there is a simple service url available for it
     * or not.
     *
     * @param id
     * @param userId
     * @return
     */
    public Future<String> getServiceUrl (int id, int userId) {
        String sql = "SELECT " +
            "khc.service_url " +
            "FROM " +
            "ktt_health_check AS khc " +
            "WHERE " +
            "id = ? and cr_user = ? " +
            "LIMIT 1";


        Future<String> result = Future.failedFuture("");
        this.mySqlPool_.withTransaction(client -> client
            .preparedQuery(sql)
            .execute(Tuple.of(id, userId))
            .onSuccess(v -> {
                System.out.println("[SM][gsu] Transaction succeeded");
                //result.otherwise(true).toCompletionStage();
                //Row converter = v.get
                if (0 < v.size()) {
                    String temp = v.iterator().next().getString("service_url");
                    result.otherwise(temp).toCompletionStage();
                } else {
                    System.out.println("[SM][gsu] no result for the query.");
                    result.otherwise("").toCompletionStage();
                }
            })
            .onFailure(err -> {
                System.out.println("[SM][gsu] error: " + err);
                result.otherwise("").toCompletionStage();
            })
        );

        return result;
    }

    /**
     *
     * @param user_name
     * @param password
     * @return
     * @todo implement it later.
     */
    public Future<Boolean> checkAuth (String user_name, String password) {
        String sql = ""; //
        Future<Boolean> result = Future.failedFuture("false");
        this.mySqlPool_.withTransaction(client -> client
            .preparedQuery(sql)
            .execute(Tuple.of(user_name, password))
            .onSuccess(v -> {
                System.out.println("[SM][UpdateExisting] Transaction succeeded");
                result.otherwise(true).toCompletionStage();
            })
            .onFailure(err -> {
                System.out.println("[SM][UpdateExisting] error: " + err);
                result.otherwise(false).toCompletionStage();
            })
        );

        return result;
    }


}
