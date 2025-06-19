package com.verticle;

import io.vertx.core.AbstractVerticle;

public class Service1 extends AbstractVerticle {
  @Override
  public void start() throws Exception {

    vertx.eventBus().consumer("s1",r->{
      System.out.println(r.body().toString());
      r.reply(r.body());
    });

  }
}
