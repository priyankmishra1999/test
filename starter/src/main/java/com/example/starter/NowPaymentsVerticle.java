package com.example.starter;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.*;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class NowPaymentsVerticle extends AbstractVerticle {

  private final HttpClientOptions options = new HttpClientOptions()
    .setSsl(true)
    .setDefaultHost("api.nowpayments.io")
    .setDefaultPort(443);

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    Router baseRouter = Router.router(vertx);
    Router router = Router.router(vertx);
    router.route("/public*").handler(BodyHandler.create());
    baseRouter.mountSubRouter("/api", router);
    router.get("/status").handler(this::statusHandler);
    router.get("/coinPrice").handler(this::coinPrice);
    vertx.createHttpServer().requestHandler(baseRouter).listen(1080, http -> {
      if (http.succeeded()) {
        startPromise.complete();
        System.out.println("HTTP server started on port 1080");
      } else {
        startPromise.fail(http.cause());
      }
    });
  }

  private void coinPrice(RoutingContext routingContext) {
    HttpClientOptions options1 = new HttpClientOptions()
      .setSsl(true)
      .setDefaultHost("api.coinpaprika.com")
      .setDefaultPort(443);
    routingContext.vertx().executeBlocking(p -> {
      HttpClient client = routingContext.vertx().createHttpClient(options1);
      client.request(HttpMethod.GET, "v1/tickers/btc-bitcoin").compose(HttpClientRequest::send).compose(HttpClientResponse::body).onComplete(buffer -> System.out.println(buffer));
    }, r -> {
      System.out.println(r);
    });
  }

  private void statusHandler(RoutingContext routingContext) {
    HttpClient client = routingContext.vertx().createHttpClient(options);
    client.request(HttpMethod.GET, "/v1/status")
      .compose(HttpClientRequest::send
      )
      .compose(HttpClientResponse::body)
      .onComplete(bufferAsyncResult -> apiResponse(bufferAsyncResult, routingContext));

  }

  private void apiResponse(AsyncResult<Buffer> ar, RoutingContext routingContext) {
    if (ar.succeeded()) {
      LoggerUtil.infoLogger(NowPaymentsVerticle.class, String.valueOf(ar.result()));
      routingContext.response().end(ar.result());
    } else {
      routingContext.response().end(ar.cause().getMessage());
    }

  }
}
