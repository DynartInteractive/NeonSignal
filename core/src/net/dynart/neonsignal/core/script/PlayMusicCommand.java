package net.dynart.neonsignal.core.script;

import net.dynart.lisa.core.Engine;
import net.dynart.lisa.core.SoundManager;
import net.dynart.lisa.core.script.Command;

public class PlayMusicCommand implements Command {

    private final SoundManager soundManager;
    private final String name;

    public PlayMusicCommand(Engine engine, String name) {
        this.name = name;
        soundManager = engine.getSoundManager();
    }

    public boolean act(float delta) {
        soundManager.playMusic(name);
        return true;
    }
}
