package pt.yquest.instances;

import io.vertx.core.cli.CLI;
import io.vertx.ext.shell.command.CommandBuilder;

public interface CommandBuilderByCLI {
    CommandBuilder create(CLI cli);
}
