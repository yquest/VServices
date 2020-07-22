package pt.yquest.commands;

import io.vertx.core.Future;
import io.vertx.core.cli.CLI;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.shell.command.Command;
import io.vertx.ext.shell.command.CommandProcess;
import io.vertx.ext.web.RoutingContext;
import pt.yquest.instances.Context;

import javax.inject.Inject;
import java.util.Optional;

public class ServicesDeployedCLI implements AppAction {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServicesDeployedCLI.class);

    private final Context context;

    @Inject
    ServicesDeployedCLI(Context context) {
        this.context = context;
    }

    public Command getCommand() {
        CLI cli = context.cliProvider().get().setName("services-deployed")
                .setDescription("Services deployed");

        return context.getCommandBuilderByCli().create(cli)
                .processHandler(this::handleProcess)
                .build(context.getVertx());
    }

    @Override
    public String getRoutPath() {
        return "/deployed";
    }

    @Override
    public HttpMethod getHttpMethod() {
        return HttpMethod.GET;
    }

    @Override
    public void routing(RoutingContext rc) {
        deployedServices()
                .onFailure(error -> AppAction.onError(LOGGER, rc, error))
                .onSuccess(ids -> rc.response().end(ids.toBuffer()));
    }


    private Future<JsonObject> deployedServices() {
        return context.getServicesDeployedJson().getDeployedIds();
    }


    private void handleProcess(CommandProcess process) {
        deployedServices().onSuccess(jo -> {
            process.write(jo.encodePrettily());
            process.write("\n");
            process.end();
        }).onFailure(error->AppAction.onError(process,error));
    }

}
