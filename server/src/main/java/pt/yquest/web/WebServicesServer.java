package pt.yquest.web;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServer;
import pt.yquest.commands.AppAction;

import java.util.List;

public interface WebServicesServer {
    void start(int port, List<AppAction> actions, Handler<AsyncResult<HttpServer>> handler);
}
