package pt.fabm;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;

@SuppressWarnings("unused") //loaded by the class name
public class MainVerticleServer extends AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(MainVerticleServer.class);

    @Override
    public void start(Promise<Void> startPromise) {
        loadServerHttp().onComplete(startPromise)
                .onSuccess(e -> LOGGER.info("loaded successfully"))
                .onFailure(e -> LOGGER.error("error", e));
    }

    private Future<Void> loadServerHttp() {
        Promise<HttpServer> serverPromise = Promise.promise();
        Router router = Router.router(vertx);
        LOGGER.info("successfully create router:{}",router);
        HttpServer server = vertx.createHttpServer();
        server.requestHandler(router).listen(
                config().getInteger("port"),
                config().getString("host"),
                serverPromise.future()
        );

        return serverPromise
                .future().flatMap(httpServer ->
                        SharedComponents.SHARED_COMPONENTS.future().map(e -> {
                            LOGGER.debug("complete router:{}",router);
                            e.getRouter().complete(router);
                            return router;
                        }).map(e -> null));
    }

    @Override
    public void stop(Promise<Void> stopFuture) {
        stopFuture.handle(Future.succeededFuture());
    }
}
