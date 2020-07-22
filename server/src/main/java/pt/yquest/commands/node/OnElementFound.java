package pt.yquest.commands.node;

@FunctionalInterface
public interface OnElementFound {
    void execute(String element);
}
