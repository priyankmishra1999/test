package com.example.starter;

import com.verticle.HttpVerticle;
import io.vertx.core.Vertx;

public class Main {
  public static void main(String[] args) {

    Vertx vertx=Vertx.vertx();
    vertx.deployVerticle(new MainVerticle());
//    vertx.deployVerticle(new HttpVerticleNew());
  }
}
