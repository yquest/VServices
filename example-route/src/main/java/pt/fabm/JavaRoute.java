package pt.fabm;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;

@SuppressWarnings("unused") //loaded by the class name
public class JavaRoute extends AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(JavaRoute.class);
    private final Promise<Route> routePromise = Promise.promise();

    @Override
    public void start(Promise<Void> startPromise) {
        SharedComponents.SHARED_COMPONENTS.future()
                .flatMap(e -> e.getRouter().future())
                .map(this::loadRoute)
                .onComplete(ar -> {
                    routePromise.handle(ar);
                    routePromise.future().map((Void)null).onComplete(startPromise);
                })
                .onSuccess(e -> {
                    LOGGER.info("loaded successfully:{}", e);
                })
                .onFailure(e -> LOGGER.error("error", e));
    }


    private Route loadRoute(Router router) {
        LOGGER.info("successfully loaded router:{}", router);
        return router.get("/example/java/get").handler(rc -> {
            HttpServerResponse response = rc.response();
            response.putHeader("content-type", "application/json");
            response.end(
                    new JsonObject()
                            .put("my", "json")
                            .toBuffer()
            );
        });
    }

    @Override
    public void stop(Promise<Void> stopFuture) {
        routePromise.future().onSuccess(Route::remove);
        stopFuture.handle(Future.succeededFuture());
    }
}
