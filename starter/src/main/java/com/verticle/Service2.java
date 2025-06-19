package com.verticle;

import io.vertx.core.AbstractVerticle;

import java.util.concurrent.atomic.AtomicLong;

public class Service2 extends AbstractVerticle {
  @Override
  public void start() throws Exception {
    vertx.eventBus().consumer("s2", r -> {
      vertx.executeBlocking(y -> {
        AtomicLong a = new AtomicLong();

        for (long i = 0; i <= 1000000000L; i++) {
          a.addAndGet(i);
        }
        y.complete(a.get());
      },res->{
        if(res.succeeded()){
          r.reply(res.result());
        }
      });


    });
  }
}
