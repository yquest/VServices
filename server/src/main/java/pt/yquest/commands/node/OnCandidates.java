package pt.yquest.commands.node;

import java.util.List;

@FunctionalInterface
public interface OnCandidates {
    void execute(List<String> elements);
}
