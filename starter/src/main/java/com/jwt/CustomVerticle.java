package com.jwt;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class CustomVerticle extends AbstractVerticle {

	@Override
	public void start(Promise<Void> startPromise) throws Exception {
		Router router = Router.router(vertx);
		router.get("/data").handler(this::handlerData);
		vertx.createHttpServer().requestHandler(router).listen(2080).onSuccess(server -> startPromise.complete())
				.onFailure(startPromise::fail);
	}

	@SuppressWarnings("deprecation")
	private void handlerData(RoutingContext routingcontext) {
		routingcontext.vertx().executeBlocking(f -> {
			try {
				sendData("send-data", routingcontext, r -> {
					if (r.get("status").equals("OK")) {
						f.complete(r.data());
					} else {
						f.fail(r.failure());
					}
				});
			} catch (Exception e) {
				f.complete(e);
			}
		}, r -> {
			if (r.succeeded()) {
				System.out.println(r.result());
				routingcontext.json(r.result());
			} else {
				routingcontext.json(r.cause());

			}
		});

	}

	@SuppressWarnings({ "deprecation", "deprecation", "deprecation" })
	private void sendData(String string, RoutingContext routingContext, Handler<RoutingContext> handler) {
	    routingContext.vertx().executeBlocking(promise -> {
	        routingContext.put("status", "OK");
//	        try {
//				Thread.sleep(10000);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
	        routingContext.put("data", "Reply from context");
	        promise.complete(routingContext);
	    }, res -> {
	        if (res.succeeded()) {
	            handler.handle((RoutingContext) res.result());
	        } else {
	            // Option 1: You must decide how to handle failure — since `handler` only takes RoutingContext
	            routingContext.put("status", "ERROR");
	            routingContext.put("message", res.cause().getMessage());
	            handler.handle(routingContext);
	        }
	    });
	}

//		r.handle(routingcontext);

}
