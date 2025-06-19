package com.example.starter;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

public class LoggerUtil {
  private static final ConcurrentHashMap<Class<?>, Logger> loggerCache = new ConcurrentHashMap<>();

  public static void infoLogger(Class<?> logClass, String message) {
    Logger log = loggerCache.computeIfAbsent(logClass, LoggerFactory::getLogger);
    log.info(message);
  }
}
