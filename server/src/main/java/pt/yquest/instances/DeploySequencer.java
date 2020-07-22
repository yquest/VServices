package pt.yquest.instances;

import io.vertx.core.Future;

import java.util.Map;

public interface DeploySequencer {
    Future<Map<String, String>> deploy(String name);
}
