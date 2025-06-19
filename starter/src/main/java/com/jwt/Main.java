package com.jwt;

import io.vertx.core.Vertx;

public class Main {

	public static void main(String... args) {

		Vertx vertex = Vertx.vertx();
//		vertex.deployVerticle(new AuthVerticle());
		vertex.deployVerticle(new CustomVerticle(), r -> {
			if (r.succeeded()) {
				System.out.println("Service deployed");
			} else {
				System.err.println("Service not deployed");
			}
		});
	}

}
