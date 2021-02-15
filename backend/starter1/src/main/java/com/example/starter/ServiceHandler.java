package com.example.starter;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.sqlclient.Tuple;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Service handler
 * This class serve as a controller layer between the routing, database and frontend.
 * This class has the purpose to check services from database time-to-time and
 * update the database.
 * 
 * @todo split this class into smaller, to achieve SOLID.
 */
public class ServiceHandler extends AbstractVerticle {
    private int scanInterval_ = 10; // seconds!
    private long timerId_;
    private int counter_ = 0;
    private Vertx vertx_; // for reverse injection -,-
    private int httpClientTimeout_ = 5; // seconds!
    private WebClient client_;
    private SimpleModel db_;

    public ServiceHandler (Vertx vertx) {
        this.vertx_ = vertx;
        System.out.println("[ServiceHandler] running.");
        this.initHttpClient();
        //this.scanServices();
    }

    public void run() {
        System.out.println("[SH][run] prepare for run, please stand by...");
        this.vertx_.setTimer(3000, v -> {
            System.out.println("[SH][run] scan services starting.");
            this.scanServices();
        });

    }

    /**
     * Setter for the database.
     *
     * @param db
     */
    public void setDatabase (SimpleModel db) {
        this.db_ = db;
    }

    /**
     * Instantiate/Prepare the internal HTTP client (vertx) for later usage.
     * The plan is to share the resource instead of creating new instances...
     */
    private void initHttpClient () {
        WebClientOptions options = new WebClientOptions()
            //.setConnectTimeout(this.httpClientTimeout_)
            .setUserAgent("KTT Health checker")
            .setKeepAlive(false) // non-active connection!
            .setSsl(true)
            //.setIdleTimeout(this.httpClientTimeout_)
            .setTrustAll(true) // because its a test!
            .setFollowRedirects(true)
        ;
        this.client_ = WebClient.create(this.vertx_, options);
    }

    private void scanServices () {
        System.out.println("[ServiceHandler][scanServices] scan starting...");

        try {
            this.timerId_ = this.vertx_.setPeriodic(TimeUnit.SECONDS.toMillis(this.scanInterval_), this::periodicTest);
        } catch (Exception err) {
            System.out.println("[SH][scan] exception: " + err.getMessage() + ", " + err.getStackTrace());
        }

    }

    private void periodicTest(Long aLong) {
        System.out.println("Hello there general kenobi! " + aLong);
        this.db_.getAllUrl().onSuccess(data -> {
            this.processRetrievedUrlsForAutoScan(data);
        })
        .onFailure(err -> {
            this.vertx_.cancelTimer(this.timerId_);
        })
        .onComplete(x -> {
            //System.out.println("[SH][pt] [onComplete] retrieved db stuff: " + x.toString());
        });
    }

    private void processRetrievedUrlsForAutoScan (JsonArray listOfUrl) {
        listOfUrl.forEach(x -> {
            JsonObject temp2 = (JsonObject) x;

            this.checkUrlByHttpClient(temp2.getString("service_url"))
                .onSuccess(checkData -> {
                    // refresh db
                    //System.out.println("[sh][pru] check");
                    //System.out.println("[sh][pru]  - Url: " + checkData.getUrl());
                    //System.out.println("[sh][pru]  - Available: " + checkData.isAvailable());
                    //System.out.println("[sh][pru]  - Response time: " + checkData.getResponseTime());
                    //System.out.println("[sh][pru]  - Error: " + checkData.isError());
                    //System.out.println("[sh][pru]  - Error message: " + checkData.getErrorMessage());
                    //System.out.println("[sh][pru]  - Status code: " + checkData.getStatusCode());
                    //System.out.println("------------------------------");
                    db_.updateOneForScan(temp2.getInteger("id"), checkData.isAvailable(), checkData.getResponseTime());
                })
                .onFailure(err -> {
                    // refresh db but different
                    // probably this shall not occure!
                    System.out.println("[sh][pru] error " + err.getMessage());
                })
            ;
        });
    }

    /**
     * Check URL by HTTP client.
     * This method shall check an URL availability. The return first part shall be the flag for availability and
     * the integer number shall be the response time in milliseconds if available.
     * This method is a promise (Future) based solution, e.g.: async.
     *
     * @param url
     * @return
     */
    public Future<resultOfHttpClientCheck> checkUrlByHttpClient (String url) {
        Future<resultOfHttpClientCheck> result = Future.future(x -> {
            long start = System.currentTimeMillis();
            resultOfHttpClientCheck temp = new resultOfHttpClientCheck();
            temp.setError(false);
            temp.setErrorMessage("");
            temp.setUrl(url);
            temp.setStatusCode(0);
            System.out.println("[sh][cubhc] try to check the URL of " + url);

            // @note alternate, non-vertx version, this solution has no problem with DNS & resolving addresses nor reach anything -,-
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setConnectTimeout((int) TimeUnit.SECONDS.toMillis(this.httpClientTimeout_));
                connection.setReadTimeout((int) TimeUnit.SECONDS.toMillis(this.httpClientTimeout_));
                connection.setInstanceFollowRedirects(true);
                connection.setRequestMethod("HEAD");
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.0; en-US; rv:1.9.1.2) Gecko/20090729 Firefox/3.5.2 (.NET CLR 3.5.30729)");
                connection.connect();
                int responseCode = connection.getResponseCode();
                temp.setStatusCode(responseCode);
                temp.setAvailable(true);
                temp.setResponseTime((int) (System.currentTimeMillis() - start));
                connection.disconnect();
                x.complete(temp);

            } catch (java.net.SocketTimeoutException e) {
                temp.setStatusCode(404);
                temp.setAvailable(false);
                temp.setError(true);
                temp.setErrorMessage("Socket timeout exception: " + e.getMessage());
                x.complete(temp);
            } catch (Exception e) {
                System.out.println("[sh][http client] exception: " + e.getMessage());
                temp.setAvailable(false);
                temp.setResponseTime((int) (System.currentTimeMillis() - start));
                temp.setError(true);
                temp.setErrorMessage("exception: " + e.getMessage());
                x.complete(temp);
            }

            /*
            // @note its ok, but DNS resolving is a mess for java & vertx, so it does not works...

            boolean urlIsSsl = false;
            int portForConnection = 80;

            try {
                URL checkUrlBase = new URL (url);
                if ("https".equals(checkUrlBase.getProtocol())) {
                    urlIsSsl = true;
                    portForConnection = 80;
                }
            } catch (MalformedURLException e) {
                //e.printStackTrace();
                System.out.println("[sh][url check][error/exception] " + e.getMessage());
            }

            this.client_
                //.get(url)
                .get(portForConnection, url, "")
                //.timeout(this.httpClientTimeout_) // force add timeout
                .ssl(urlIsSsl)
                .send()
                .onSuccess(res -> {
                    System.out.println("[sh][cubhc] url of " + url + "it works, status code: " + res.statusCode());
                    temp.setAvailable(true);
                    temp.setStatusCode(res.statusCode());
                    temp.setResponseTime((int) (System.currentTimeMillis() - start));
                    //result.otherwise(temp).toCompletionStage();
                    //x.complete(temp);
                })
                .onFailure(err -> {
                    System.out.println("[sh][cubhc] Url of " + url + " does not works, because: " + err);
                    temp.setAvailable(false);
                    temp.setError(true);
                    temp.setErrorMessage(err.getMessage());
                    temp.setResponseTime((int) (System.currentTimeMillis() - start));
                    //result.otherwise(temp).toCompletionStage();
                })
                .onComplete(y -> {
                    x.complete(temp); // we does not throw or add a fail instead populating a result object!
                })
            */
            ;
        });

        return result;
    }
}
