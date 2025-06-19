package com.example.starter;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class EventBusService extends AbstractVerticle {
  @Override
  public void start(Promise<Void> startPromise) throws Exception {


    Router router = Router.router(vertx);
    router.post("/send").handler(this::sendData);
    vertx.createHttpServer().requestHandler(router).listen(1090, http -> {
      if (http.succeeded()) {
        startPromise.complete();
        LoggerUtil.infoLogger(EventBusService.class, "HTTP server started on port 1090");
      } else {
        startPromise.fail(http.cause());
      }
    });

  }

  //    private void sendData(RoutingContext routingContext) {
//      String data = routingContext.request().getParam("id");
//      final EventBus sending = vertx.eventBus().send("sending", data);
//
//    }
//  private void sendData(RoutingContext routingContext) {
//    String data = routingContext.request().getParam("id");
//
//    vertx.eventBus().<String>request("sending", data, reply -> {
//      if (reply.succeeded()) {
//        routingContext.response()
//          .putHeader("content-type", "application/json")
//          .end(new JsonObject().put("reply", reply.result().body()).toBuffer());
//      } else {
//        routingContext.response()
//          .setStatusCode(500)
//          .end(new JsonObject().put("error", reply.cause().getMessage()).toBuffer());
//      }
//    });
//  }
  private void sendData(RoutingContext routingContext) {
    String data = routingContext.request().getParam("id");

    // Send data to background consumer
    vertx.eventBus().send("sending", data);

    // Immediately return response
    routingContext.response()
      .putHeader("content-type", "application/json")
      .end(new JsonObject()
        .put("status", "success")
        .put("message", "Data sent to background processor")
        .toBuffer());
  }


}
