package net.dynart.neonsignal.core.script;

import net.dynart.neonsignal.core.Engine;
import net.dynart.neonsignal.core.SoundManager;

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
