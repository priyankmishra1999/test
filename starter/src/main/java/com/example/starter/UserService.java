package com.example.starter;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;

import java.security.PublicKey;

public class UserService extends AbstractVerticle {
  private final KeyService keyService;

  public UserService() {
    this.keyService = new KeyService();
  }


  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    Router router = Router.router(vertx);
    router.route().handler(CorsHandler.create("http://localhost:4200").allowedMethod(HttpMethod.GET).allowedMethod(HttpMethod.POST).allowedMethod(HttpMethod.PUT).allowedMethod(HttpMethod.DELETE)
      .allowCredentials(true));

    router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));

    router.route("/post").handler(context -> {
      System.out.println("Success");
      context.json(new JsonObject().put("message", "Success"));
    });

    router.get("/public-key").handler(r -> {
      try {
        String data = r.request().getParam("id");
        PublicKey publicKey = keyService.createSessionKeyPair(data);
        r.json(new JsonObject().put("sessionKey", keyService.convertPublicKeyToBase64(publicKey)));
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });
    router.get("/encoded").handler(r -> {
      String data = r.request().getParam("value");
      String publicKeyBase64 = r.request().getHeader("sessionKey");

      r.vertx().executeBlocking(promise -> {
        // This runs on a worker thread
        String encodedData = keyService.getEncodedData(publicKeyBase64, data);
        promise.complete(encodedData);
      }, res -> {

        // This runs back on the event loop thread
        if (res.succeeded()) {
          System.out.println(Thread.currentThread().getName()); // Event loop thread
          r.json(new JsonObject().put("data", res.result()));
        } else {
          r.fail(res.cause());
        }
      });
    });

    // set the session id in the data base or redis with the user id or any unique id to clear the redis after the session is expire
    router.get("/decoded").handler(r -> {
      String data = r.request().getParam("value");
      String key = r.request().getParam("key");
//      System.out.println(data);
      System.out.println(Thread.currentThread().getName());


      keyService.decryptedData(data, key).onSuccess(x -> {
        System.out.println(Thread.currentThread().getName());

        r.json(new JsonObject().put("data", x));
      }).onFailure(f -> r.response().end("Error"));
    });

    router.get("/get-public").handler(this::publicVerticle);
    vertx.createHttpServer().requestHandler(router).listen(2000, http -> {
      if (http.succeeded()) {
        startPromise.complete();
        System.out.println("HTTP server started on port 2000");
      } else {
        startPromise.fail(http.cause());
      }
    });
  }

  private void publicVerticle(RoutingContext routingContext) {
    String id = routingContext.request().getParam("id");
    if (id == null || id.isEmpty()) {
      routingContext.response()
        .setStatusCode(400)
        .end("Missing 'id' query parameter");
      return;
    }

    HttpClient client = routingContext.vertx().createHttpClient();

    client.request(HttpMethod.GET, 2000, "localhost", "/public-key?id=" + id)
      .compose(req -> req.send().compose(HttpClientResponse::body))
      .onComplete(ar -> publicKeyHandler(ar, routingContext));

  }

  private synchronized void publicKeyHandler(AsyncResult<Buffer> ar, RoutingContext routingContext) {
    if (ar.succeeded()) {
      Buffer responseBuffer = ar.result();
      routingContext.response()
        .putHeader("Content-Type", "application/json")
        .end(responseBuffer);
    } else {
      routingContext.response()
        .setStatusCode(500)
        .end("Failed to retrieve public key: " + ar.cause().getMessage());
    }
  }

}
