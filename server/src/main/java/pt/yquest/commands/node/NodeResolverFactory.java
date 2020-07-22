package pt.yquest.commands.node;

public interface NodeResolverFactory {
    NodeResolver create(String path);
}
