package pt.yquest;

import io.vertx.core.Promise;
import io.vertx.ext.web.Router;

public interface SharedComponents {
    Promise<SharedComponents> SHARED_COMPONENTS = Promise.promise();

    Promise<Router> getRouter();
}
