package pt.yquest.commands;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import pt.yquest.CachedReader;
import pt.yquest.instances.Context;
import pt.yquest.instances.ServiceTypeCreator;

import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import static pt.yquest.instances.ContextModule.PATH_TO_SAVE;


public interface ServicesDeployedJson {
    Logger LOGGER = LoggerFactory.getLogger(ServicesDeployedJson.class);

    Context getContext();

    default Future<Map<String, String>> redeployJson(String name) {
        return undeployService(name)
                .flatMap(e -> deployFromServices(name));
    }

    default Future<Void> undeployService(String name) {
        Promise<Void> undeployVericle = Promise.promise();
        return getContext().getServicesDeployedJson().getDeployedIds()
                .flatMap(ids -> Context.require(ids, "ids map is empty"))
                .map(ids -> ids.getString(name))
                .flatMap(id -> Context.require(
                        id,
                        String.format("There is no '%s' service deployed", name)
                ))
                .flatMap(id -> {
                    getContext().getVertx().undeploy(id, undeployVericle);
                    return undeployVericle.future().map(ignore -> id);
                })
                .flatMap(id -> unregisterServiceDeployed(name, id));
    }

    default Future<JsonObject> getDeployedIds() {
        return getContext().getPathToSave().getCache();
    }

    default Future<String> serviceJsonDeploy(String name, JsonObject service) {
        Objects.requireNonNull(service);

        final Iterator<ServiceTypeCreator> iterator = getContext().getServiceTypeCreatorIterator();

        ServiceTypeCreator serviceTypeCreator = null;
        while (iterator.hasNext()) {
            serviceTypeCreator = iterator.next();
            serviceTypeCreator.setService(service);
            if (serviceTypeCreator.matchCreator()) {
                break;
            }
        }
        assert serviceTypeCreator != null;
        serviceTypeCreator.setContext(getContext());

        LOGGER.debug("create service {}", service.getString("main"));
        return serviceTypeCreator.createVerticle(name);
    }

    default Future<Void> unregisterServiceDeployed(String name, String id) {
        CachedReader<JsonObject> pathToSave = getContext().getPathToSave();
        return pathToSave.getCache().flatMap(ids->{
            if (!ids.containsKey(name)) {
                return Future.failedFuture(
                        String.format("there is no '%s' service to undeploy", name)
                );
            } else if (!ids.getString(name).equals(id)) {
                return Future.failedFuture(
                        String.format("the id[%s] doesn't corresponds to the name %s", id, name)
                );
            }
            if (!ids.getString(name).equals(id)) {
                return Future.failedFuture(String.format("the id[%s] doesn't corresponds to the name %s", id, name));
            }
            ids.remove(name);
            return pathToSave.updateAsync(ids);
        });
    }

    default Future<Void> registerServicesDeployed(Map<String, String> map) {
        Promise<JsonObject> asyncFileRead = Promise.promise();
        if (PATH_TO_SAVE != null && Paths.get(PATH_TO_SAVE).toFile().exists()) {
            Promise<Buffer> bufferRead = Promise.promise();
            getContext().getVertx().fileSystem().readFile(PATH_TO_SAVE, bufferRead);
            bufferRead.future()
                    .map(Buffer::toJsonObject)
                    .onComplete(asyncFileRead);
        } else {
            asyncFileRead.fail("path file doesn't exists:" + PATH_TO_SAVE);
        }

        Promise<Void> promise = Promise.promise();
        asyncFileRead.future().onFailure(promise::fail);
        asyncFileRead.future().onSuccess(loadedMap -> {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                loadedMap.put(entry.getKey(), entry.getValue());
            }
            getContext().getVertx().fileSystem().writeFile(PATH_TO_SAVE, loadedMap.toBuffer(), promise);
        });
        return promise.future().map(ignore -> {
            getContext().getPathToSave().reset();
            return null;
        });
    }

    default Future<JsonObject> getDeployedServices() {
        return getContext().getPathToSave().getCache();
    }

    default Future<Map<String, String>> deployFromServices(String name) {
        JsonObject services = getContext().getVerticleList();
        JsonObject service = services.getJsonObject(name);
        if (service == null) {
            return Future.failedFuture("service with name " + name + " not found\n");
        }

        return getContext().getDeploySequencer().deploy(name)
                .flatMap(map -> {
                    Promise<Void> promise = Promise.promise();
                    registerServicesDeployed(map).onComplete(promise);
                    return promise.future().map(map);
                });
    }
}
