package pt.yquest.instances;

import dagger.Component;
import io.vertx.core.*;
import io.vertx.core.cli.CLI;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import pt.yquest.Application;
import pt.yquest.CachedReader;
import pt.yquest.commands.ServicesDeployedJson;
import pt.yquest.commands.node.NodeComplete;
import pt.yquest.web.WebServicesServer;

import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.Iterator;
import java.util.function.Function;

@Singleton
@Component(modules = {ContextModule.class})
public interface Context {
    Logger LOGGER = LoggerFactory.getLogger(Context.class);

    static <T> Handler<AsyncResult<T>> logOnFinish(Class<?> klass) {
        return ar -> {
            if (ar.failed()) {
                LOGGER.error("error on class:" + klass.getCanonicalName(), ar.cause());
            } else {
                LOGGER.trace("successfully finished on class:" + klass.getCanonicalName());
            }
        };
    }

    static <T> Future<T> require(T obj, String message) {
        Promise<T> promise = Promise.promise();
        if (obj == null) {
            promise.fail(message);
        } else {
            promise.complete(obj);
        }
        return promise.future();
    }

    Vertx getVertx();

    CachedReader<JsonObject> getPathToSave();

    ServicesDeployedJson getServicesDeployedJson();

    WebServicesServer getWSS();

    Function<String, Future<JsonObject>> getConfYml();

    Application getApp();

    JsonObject getVerticleList();

    CommandBuilderByCLI getCommandBuilderByCli();

    Provider<CLI> cliProvider();

    NodeComplete getNodeComplete();

    ServiceTypeRegister getServiceRegister();

    Iterator<ServiceTypeCreator> getServiceTypeCreatorIterator();

    DeploySequencer getDeploySequencer();
}
