package pt.fabm;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;

@SuppressWarnings("unused")//loaded by vertx
public class DefaultSharedComponents extends AbstractVerticle implements SharedComponents {
    Logger LOGGER = LoggerFactory.getLogger(DefaultSharedComponents.class);
    private final Promise<Router> routerPromise = Promise.promise();

    @Override
    public void start(Promise<Void> startPromise) {
        SharedComponents.SHARED_COMPONENTS.complete(this);
        SharedComponents.SHARED_COMPONENTS.future().<Void>map(e -> null)
                .onComplete(startPromise);
    }

    @Override
    public Promise<Router> getRouter() {
        LOGGER.debug("router:{}", routerPromise);
        return routerPromise;
    }
}
