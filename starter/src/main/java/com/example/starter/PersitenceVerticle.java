package com.example.starter;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;

public class PersitenceVerticle extends AbstractVerticle {
  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    EventBus eventBus = vertx.eventBus();
    final MessageConsumer<Object> sending = eventBus.consumer("sending");
    sending.handler(this::sendingHandler);
  }

  private void sendingHandler(Message<Object> objectMessage) {
    vertx.executeBlocking(promise -> {
      for (int i = 0; i < 100; i++) {
//        try {
//          Thread.sleep(1000);
//        } catch (InterruptedException e) {
//          throw new RuntimeException(e);
//        }
        LoggerUtil.infoLogger(PersitenceVerticle.class, objectMessage.body().toString()+" " +i+" " + Thread.currentThread().getName());
      }
      promise.complete();
    });

  }
}
