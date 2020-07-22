package pt.yquest;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import pt.yquest.instances.Context;
import pt.yquest.instances.ServiceTypeCreator;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Optional;

public class GroovyDslServiceType implements ServiceTypeCreator {
    private JsonObject service;
    private Context context;

    @Override
    public boolean matchCreator() {
        return Optional.ofNullable(service.getString("service-type"))
                .filter(e -> e.equals("groovy-route-dsl"))
                .isPresent();
    }

    @Override
    public Future<String> createVerticle(String name) {
        Promise<String> idPromise = Promise.promise();
        JsonObject optionsJson = service.getJsonObject("options");
        File file = new File(service.getString("main"));
        if(!file.exists()){
            return Future.failedFuture(new FileNotFoundException(file.getAbsolutePath()));
        }
        DeploymentOptions options;
        if (optionsJson != null) {
            options = new DeploymentOptions();
            options.fromJson(optionsJson);
        } else {
            options = new DeploymentOptions();
        }


        context.getVertx().deployVerticle(new GroovyRouterDslVerticle(file), options, idPromise);
        return idPromise.future();
    }

    @Override
    public Context getContext() {
        return context;
    }

    @Override
    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public JsonObject getService() {
        return service;
    }

    @Override
    public void setService(JsonObject service) {
        this.service = service;
    }
}
