package com.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Verticle;

public class Service3 extends AbstractVerticle {
  @Override
  public void start() throws Exception {
    vertx.eventBus().consumer("s3",r->{
      r.reply(r.body());
    });
  }
}
