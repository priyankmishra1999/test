package com.verticle;

import io.vertx.core.AbstractVerticle;

public class HttpVerticle extends AbstractVerticle {
  @Override
  public void start() throws Exception {
    vertx.deployVerticle(new Service1());
    vertx.deployVerticle(new Service2());
    vertx.deployVerticle(new Service3());
    vertx.deployVerticle(new CallBackHandlerVerticle());
  }
}
