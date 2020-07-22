package pt.yquest.instances;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import javax.inject.Inject;
import java.util.*;

/**
 * @author fmonteir
 * @version ($Revision$ $Date$)
 */
public class DeploySequencerImp implements DeploySequencer {
    private final Map<String, String> result = new HashMap<>();
    private final Context context;
    private Iterator<Map.Entry<String, JsonObject>> toDeploy;
    private JsonObject deployed;
    private final Promise<Void> finalPromise = Promise.promise();

    @Inject
    public DeploySequencerImp(Context context) {
        this.context = context;
    }

    private void doDeploy() {
        if (!toDeploy.hasNext()) {
            finalPromise.complete();
            return;
        }
        var current = toDeploy.next();

        context.getServicesDeployedJson().serviceJsonDeploy(current.getKey(), current.getValue())
                .onFailure(finalPromise::fail)
                .onSuccess(id -> {
                    result.put(current.getKey(), id);
                    doDeploy();
                });
    }

    private List<Map.Entry<String, JsonObject>> getDependencies(String name) {
        if (deployed.containsKey(name)) {
            return List.of();
        }
        if (!context.getVerticleList().containsKey(name)) {
            return List.of();
        }
        JsonObject service = context
                .getVerticleList()
                .getJsonObject(name);

        List<Map.Entry<String, JsonObject>> list = new ArrayList<>();
        JsonArray currentDependencies = service.getJsonArray("dependencies", new JsonArray());
        for (int i = 0; i < currentDependencies.size(); i++) {
            var dependency = currentDependencies.getString(i);
            list.addAll(getDependencies(dependency));
        }
        list.add(Map.entry(name, service));
        return list;
    }

    /**
     * @return value of result
     */
    @Override
    public Future<Map<String, String>> deploy(String name) {
        return context.getServicesDeployedJson().getDeployedIds().flatMap(deployed -> {
            if (deployed.containsKey(name)) {
                return Future.succeededFuture(Map.of());
            }
            this.deployed = deployed;
            toDeploy = getDependencies(name).iterator();
            doDeploy();
            return finalPromise.future().map(ignore -> Map.copyOf(result));
        });
    }
}
