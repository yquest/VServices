package pt.yquest;

import dagger.Component;
import pt.yquest.instances.ServiceTypeRegister;

import java.util.function.Consumer;

@Component(modules = AppModule.class)
public interface App {
    ServiceTypeRegister getServiceRegister();
    Consumer<String> run();
}
