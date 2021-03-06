package pt.yquest.instances;

import dagger.Module;
import dagger.Provides;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.ServiceHelper;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.cli.CLI;
import io.vertx.core.cli.impl.DefaultCLI;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.spi.VertxFactory;
import io.vertx.ext.shell.command.CommandBuilder;
import pt.yquest.Application;
import pt.yquest.CachedReader;
import pt.yquest.commands.ActionsSelectorModule;
import pt.yquest.commands.ServicesDeployedJson;
import pt.yquest.commands.node.FileNodeComplete;
import pt.yquest.commands.node.FileNodeResolver;
import pt.yquest.commands.node.NodeComplete;
import pt.yquest.commands.node.NodeResolverFactory;
import pt.yquest.web.WebServicesServer;
import pt.yquest.web.WebServicesServerImp;

import javax.inject.Inject;
import java.nio.file.Paths;
import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Function;
import java.util.function.Supplier;

@Module(includes = ActionsSelectorModule.class)
public class ContextModule {
    public static final String PATH_TO_SAVE;
    private static final Logger LOGGER = LoggerFactory.getLogger(ContextModule.class);

    static {
        String PATH_TO_SAVE_SERVICES_MAP = "PATH_TO_SAVE_SERVICES_MAP";
        PATH_TO_SAVE = System.getProperty(PATH_TO_SAVE_SERVICES_MAP);
    }

    private final Vertx vertx;
    private final CachedReader<JsonObject> pathToSaveFile;
    private final NodeResolverFactory nodeResolverFactory;
    private final Deque<ServiceTypeCreator> serviceTypeCreatorDeque;

    @Inject
    public ContextModule() {
        vertx = ServiceHelper.loadFactory(VertxFactory.class).vertx();
        nodeResolverFactory = FileNodeResolver::new;
        if (!Paths.get(PATH_TO_SAVE).toFile().exists()) {
            LOGGER.error("path doesn't exists {0}", PATH_TO_SAVE);
        }

        Function<Buffer, Promise<Void>> writeFile = buffer -> {
            Promise<Void> promise = Promise.promise();
            vertx.fileSystem().writeFile(PATH_TO_SAVE, buffer, promise);
            return promise;
        };

        Supplier<Promise<Buffer>> readFile = () -> {
            Promise<Buffer> promise = Promise.promise();
            vertx.fileSystem().readFile(PATH_TO_SAVE, promise);
            return promise;
        };

        pathToSaveFile = new CachedReader<>(
                1000 * 60 * 60,
                Buffer::toJsonObject,
                JsonObject::toBuffer,
                writeFile,
                readFile
        );

        serviceTypeCreatorDeque = new ConcurrentLinkedDeque<>();
        serviceTypeCreatorDeque.add(new DefaultServiceTypeCreator());
    }

    private Future<JsonObject> getConfYml(String path) {
        ConfigStoreOptions configStoreOptions = new ConfigStoreOptions()
                .setType("file")
                .setFormat("yaml")
                .setConfig(new JsonObject().put("path", path));

        ConfigRetriever retriever = ConfigRetriever.create(
                vertx,
                new ConfigRetrieverOptions().addStore(configStoreOptions)
        );
        Promise<JsonObject> promise = Promise.promise();
        retriever.getConfig(promise);
        return promise.future();
    }

    @Provides
    Function<String, Future<JsonObject>> providesConfYml() {
        return this::getConfYml;
    }

    @Provides
    Vertx providesVertx() {
        return vertx;
    }

    @Provides
    CachedReader<JsonObject> providesPathToSaveFile() {
        return pathToSaveFile;
    }

    @Provides
    WebServicesServer providesWebServicesServer(WebServicesServerImp wss) {
        return wss;
    }

    @Provides
    Application providesApp(ApplicationImp app) {
        return app;
    }

    @Provides
    ServicesDeployedJson providesServiceDeployedJson(Context context) {
        return () -> context;
    }

    @Provides
    JsonObject getVerticles(Vertx vertx) {
        return vertx.sharedData()
                .<String, JsonObject>getLocalMap("services")
                .get("list");
    }

    @Provides
    public NodeResolverFactory getNodeResolverFactory() {
        return nodeResolverFactory;
    }

    @Provides
    CommandBuilderByCLI providesCommandBuilderByCLI() {
        return CommandBuilder::command;
    }

    @Provides
    CLI providesCLI() {
        return new DefaultCLI();
    }

    @Provides
    NodeComplete getNodeComplete(FileNodeComplete nodeComplete) {
        return nodeComplete;
    }

    @Provides
    ServiceTypeRegister getServiceTypeRegister() {
        return serviceTypeCreatorDeque::push;
    }

    @Provides
    Iterator<ServiceTypeCreator> getServiceTypeCreatorIterator() {
        return serviceTypeCreatorDeque.iterator();
    }

    @Provides
    DeploySequencer getDeploySequencer(Context context) {
        return new DeploySequencerImp(context);
    }
}
