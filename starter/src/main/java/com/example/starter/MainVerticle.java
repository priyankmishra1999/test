package com.example.starter;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;

public class MainVerticle extends AbstractVerticle {

  @Override
  public void start(Promise<Void> startPromise) throws Exception {

//    vertx.deployVerticle(new NowPaymentsVerticle(), ar -> {
//      if (ar.succeeded()) {
//        System.out.println("Success");
//        startPromise.complete();
//      } else {
//        startPromise.fail(ar.cause());
//      }
//    });
    CompositeFuture
      .all(

        deployVerticle(RedisConnectionVerticle.class.getName())
        , deployVerticle(NowPaymentsVerticle.class.getName()),
        deployVerticle(UserService.class.getName())
        ,
        deployVerticle(EventBusService.class.getName()),
        deployVerticle(PersitenceVerticle.class.getName()))
      .onComplete(c -> {
        if (c.succeeded()) {
          System.out.println("Deployed");
        } else {
          System.out.println("Failure");
        }
      });
//    vertx.deployVerticle(new UserService(), stringAsyncResult ->
//    {
//      if (stringAsyncResult.succeeded()) {
//        System.out.println("User service deployed");
//        startPromise.complete();
//      } else {
//        startPromise.fail(stringAsyncResult.cause());
//      }
//    });
  }

  private Future<Void> deployVerticle(String verticleName) {
    Promise<Void> future = Promise.promise();
    vertx
      .deployVerticle(verticleName, er -> {
        if (er.succeeded()) {
          future.complete();
        } else {
          future.fail(er.cause());
        }
      });
    return future.future();
  }
}
