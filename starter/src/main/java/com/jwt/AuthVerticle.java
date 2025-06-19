package com.jwt;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.JWTAuthHandler;

public class AuthVerticle extends AbstractVerticle {

	private JWTAuth jwtAuth;

	@Override
	public void start(Promise<Void> startPromise) {
		Router router = Router.router(vertx);

//	    jwtAuth = JWTAuth
//	    		.create(vertx, new JWTAuthOptions().addJwk(new JsonObject())
//	      .addSecret("my-secret-key")
//	      .setJWTOptions(new JWTOptions().setExpiresInSeconds(600)));

		jwtAuth = JWTAuth.create(vertx,
				new JWTAuthOptions().addJwk(new JsonObject().put("kty", "oct").put("k", "your-base64-secret")
						.put("alg", "HS256").put("use", "sig"))
						.setJWTOptions(new JWTOptions().setExpiresInSeconds(600)));
		router.route().handler(BodyHandler.create()); // ✅ Required for JSON body parsing

		// Login Route
		router.post("/login").handler(this::handleLogin);
		// Protected API Route
		router.get("/api/data").handler(JWTAuthHandler.create(jwtAuth)).handler(this::handleSecureApi);

		// Token Refresh
		router.post("/refresh").handler(this::handleRefreshToken);

		vertx.createHttpServer().requestHandler(router).listen(8888).onSuccess(server -> startPromise.complete())
				.onFailure(startPromise::fail);
	}

	@SuppressWarnings("deprecation")
	private void handleLogin(RoutingContext ctx) {
		ctx.vertx().executeBlocking(f -> {
			try {
				JsonObject credentials = ctx.body().asJsonObject();
				String email = credentials.getString("email");
				String password = credentials.getString("password");

				// 🔐 Fake user validation
				if ("test@example.com".equals(email) && "123456".equals(password)) {
					JsonObject payload = new JsonObject().put("email", email).put("role", "user");
					String accessToken = jwtAuth.generateToken(payload, new JWTOptions().setExpiresInSeconds(300));
					String refreshToken = jwtAuth.generateToken(payload, new JWTOptions().setExpiresInSeconds(1800));

					ctx.json(new JsonObject().put("accessToken", accessToken).put("refreshToken", refreshToken));
				} else {
					ctx.response().setStatusCode(401).end("Invalid credentials");
				}
			} catch (Exception e) {
				f.fail(e);
			}

		}, res -> {
			if (res.succeeded()) {
				ctx.end(res.result().toString());
			} else {
				ctx.end(res.cause().toString());
			}
		});

	}

	private void handleSecureApi(RoutingContext ctx) {
		User user = ctx.user();
		String role = user.principal().getString("role");

		if ("user".equals(role)) {
			ctx.response().end("Secure Data for USER");
		} else {
			ctx.response().setStatusCode(403).end("Forbidden");
		}
	}

	@SuppressWarnings("deprecation")
	private void handleRefreshToken(RoutingContext ctx) {
		String refreshToken = ctx.body().asJsonObject().getString("refreshToken");

		jwtAuth.authenticate(new JsonObject().put("token", refreshToken), res -> {
			if (res.succeeded()) {
				JsonObject userPayload = res.result().principal();
				String newAccessToken = jwtAuth.generateToken(userPayload, new JWTOptions().setExpiresInSeconds(300));
				ctx.json(new JsonObject().put("accessToken", newAccessToken));
			} else {
				ctx.response().setStatusCode(401).end("Invalid refresh token");
			}
		});
	}

}
