package pt.yquest.commands;

import io.vertx.core.Future;
import io.vertx.core.cli.Argument;
import io.vertx.core.cli.CLI;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.shell.cli.Completion;
import io.vertx.ext.shell.command.Command;
import io.vertx.ext.shell.command.CommandProcess;
import io.vertx.ext.web.RoutingContext;
import pt.yquest.instances.Context;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DeployServiceCLI implements AppAction {
    private static final Logger LOGGER = LoggerFactory.getLogger(DeployServiceCLI.class);

    private final Context context;

    @Inject
    DeployServiceCLI(Context context) {
        this.context = context;
    }

    public Command getCommand() {
        final Argument argument = new Argument()
                .setArgName("name")
                .setRequired(true)
                .setIndex(0);
        CLI cli = context.cliProvider().get().setName("service-deploy")
                .setDescription("Deploy service")
                .setArguments(Collections.singletonList(argument));

        return context.getCommandBuilderByCli().create(cli)
                .processHandler(this::handleProcess)
                .completionHandler(this::handleCompletion)
                .build(context.getVertx());
    }

    @Override
    public String getRoutPath() {
        return "/deploy/:service";
    }

    @Override
    public HttpMethod getHttpMethod() {
        return HttpMethod.GET;
    }

    @Override
    public void routing(RoutingContext rc) {
        deployService(rc.pathParam("service"))
                .onSuccess(map -> rc.response().end(JsonObject.mapFrom(map).toBuffer()))
                .onFailure(error -> AppAction.onError(LOGGER, rc, error));
    }

    private Future<Map<String,String>> deployService(String name) {
        return context.getServicesDeployedJson()
                .getDeployedServices()
                .flatMap(jsonObject -> {
                    if (jsonObject.containsKey(name)) {
                        return Future.failedFuture(
                                String.format("the servcie '%s' is already installed", name)
                        );
                    }
                    return context.getServicesDeployedJson().deployFromServices(name);
                });
    }

    private void handleProcess(CommandProcess process) {
        String name = process.args().get(0);
        context.getServicesDeployedJson()
                .deployFromServices(name)
                .onFailure(error -> AppAction.onError(process, error))
                .onSuccess(id -> {
                    process.write(String.format("successfully deployed service %s\n", id));
                    process.end();
                });
    }

    private void handleCompletion(Completion completion) {
        final List<String> list = context.getVerticleList()
                .stream()
                .map(Map.Entry.class::cast)
                .map(Map.Entry::getKey)
                .map(Object::toString)
                .collect(Collectors.toList());
        CompletionList completionList = new CompletionList(1, completion);
        completionList.handle(Future.succeededFuture(list));
    }

}
