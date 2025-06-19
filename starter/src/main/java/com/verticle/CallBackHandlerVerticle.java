package com.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.Observable;

public class CallBackHandlerVerticle extends AbstractVerticle {


  @Override
  public void start() throws Exception {
    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());
    router.get("/data").handler(this::conctedData);
    vertx.createHttpServer().requestHandler(router).listen(3000);
  }

  private void conctedData(RoutingContext routingContext) {

//    Observable
    vertx.eventBus().request("s1", "data-s1", r1 -> {
      String rs1 = r1.result().body().toString();

      vertx.eventBus().request("s2", "data-s2", r2 -> {
        String rs2 = r2.result().body().toString();
//        String combine = rs1.concat(rs2);
        JsonArray jsonArray=new JsonArray();
        jsonArray.add(rs1);
        jsonArray.add(Long.valueOf(rs2));
//        JsonObject combine=new JsonObject();
//        combine.put("data",)
        vertx.eventBus().request("s3", jsonArray, r3 -> {
          routingContext.json(JsonObject.of("data", jsonArray));
        });
      });
    });

//vertx.setPeriodic(3000,
//  t->vertx.eventBus().request("s1","Hi There")
//  );
  }
}
