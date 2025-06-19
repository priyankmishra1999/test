package com.example.starter;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisConnection;
import io.vertx.redis.client.RedisOptions;

public class RedisConnectionVerticle extends AbstractVerticle {
  private static RedisConnection redisConnection = null;

  public static RedisConnection getRedisConnection() {
    return redisConnection;
  }

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    RedisOptions options = new RedisOptions()
      .setConnectionString("redis://localhost:6379");

    Redis client = Redis.createClient(vertx, options);

    client.connect(redisConnectionAsyncResult -> {
      if (redisConnectionAsyncResult.succeeded()) {
        redisConnection = redisConnectionAsyncResult.
          result();
        LoggerUtil.infoLogger(RedisConnectionVerticle.class, "Connection to redis established");
      } else {
        LoggerUtil.infoLogger(RedisConnectionVerticle.class, "Connection to redis fail");
      }
    });
  }
}
