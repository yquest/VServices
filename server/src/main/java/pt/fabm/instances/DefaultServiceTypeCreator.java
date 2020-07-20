package pt.fabm.instances;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.io.File;

public class DefaultServiceTypeCreator implements ServiceTypeCreator {
    private JsonObject service;
    private Context context;

    @Override
    public Context getContext() {
        return context;
    }

    @Override
    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public boolean matchCreator() {
        return true;
    }


    @Override
    public Future<String> createVerticle(String name) {
        return createVerticleInstance();
    }

    @Override
    public JsonObject getService() {
        return service;
    }

    @Override
    public void setService(JsonObject service) {
        this.service = service;
    }

    private Future<String> createVerticleInstance() {
        Vertx vertx = context.getVertx();
        Promise<String> promise = Promise.promise();
        JsonObject jsonOptions = service.getJsonObject("options");
        DeploymentOptions deploymentOptions;
        if (jsonOptions != null) {
            deploymentOptions = new DeploymentOptions(jsonOptions);
            if (deploymentOptions.getExtraClasspath() != null) {
                for (String lPath : deploymentOptions.getExtraClasspath()) {
                    if (!new File(lPath).exists()) {
                        String msg = "the classpath " + lPath + " doesn't exist\n";
                        promise.fail(msg);
                        return promise.future();
                    }
                }
            }
        } else {
            deploymentOptions = null;
        }
        String main = service.getString("main");
        if (deploymentOptions == null) {
            vertx.deployVerticle(main, promise);
        } else {
            vertx.deployVerticle(main, deploymentOptions, promise);
        }
        return promise.future();
    }
}
