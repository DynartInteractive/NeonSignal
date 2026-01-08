package net.dynart.neonsignal.core.script;

import java.util.LinkedList;
import java.util.List;

public class ParallelCommand implements Command {

    private final List<Command> commands = new LinkedList<>();
    private final List<Command> remove = new LinkedList<>();

    public boolean act(float delta) {
        boolean result = true;
        if (commands.isEmpty()) {
            return result;
        }
        for (Command command : commands) {
            if (command.act(delta)) {
                remove.add(command);
            } else {
                result = false;
            }
        }
        for (Command command : remove) {
            commands.remove(command);
        }
        return result;
    }

    public void init(List<Command> command) {
        remove.clear();
        commands.clear();
        commands.addAll(command);
    }

}
