package net.dynart.neonsignal.core.script;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class SequenceCommand implements Command {

    private final List<Command> commands = new LinkedList<>();

    private Iterator<Command> it;
    private Command current;

    public boolean act(float delta) {
        if (current == null) {
            return true;
        }
        if (current.act(delta)) {
            current = it.hasNext() ? it.next() : null;
            return act(delta); // be aware: can be a stack overflow
        }
        return false;
    }

    public void init(List<Command> command) {
        current = null;
        commands.clear();
        commands.addAll(command);
        it = commands.iterator();
        if (it.hasNext()) {
            current = it.next();
        }
    }

}
