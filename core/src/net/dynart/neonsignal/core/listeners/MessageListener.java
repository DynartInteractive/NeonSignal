package net.dynart.neonsignal.core.listeners;

import net.dynart.neonsignal.core.Entity;

public interface MessageListener {
    void receive(Entity sender, String message);
}
