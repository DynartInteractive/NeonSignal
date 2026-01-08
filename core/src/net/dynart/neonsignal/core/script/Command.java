package net.dynart.neonsignal.core.script;

public interface Command {
    boolean act(float delta); // returns true if finished
}
