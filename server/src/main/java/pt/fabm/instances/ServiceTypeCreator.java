package pt.fabm.instances;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public interface ServiceTypeCreator {
    Logger LOGGER = LoggerFactory.getLogger(ServiceTypeCreator.class);

    Context getContext();

    void setContext(Context context);

    boolean matchCreator();

    Future<String> createVerticle(String name);

    JsonObject getService();

    void setService(JsonObject service);

}