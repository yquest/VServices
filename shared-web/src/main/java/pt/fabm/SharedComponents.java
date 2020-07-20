package pt.fabm;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;

public interface SharedComponents {
    Promise<SharedComponents> SHARED_COMPONENTS = Promise.promise();

    Promise<Router> getRouter();
}
