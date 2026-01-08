package net.dynart.neonsignal.core.script;

import net.dynart.neonsignal.core.script.Command;
import net.dynart.neonsignal.core.Engine;
import net.dynart.neonsignal.screens.CutsceneScreen;

public class SayCommand implements Command {

    private final CutsceneScreen cutsceneScreen;
    private final String name;
    private final String text;

    private boolean first = true;
    private final boolean finish;
    private final boolean start;
    private final boolean left;

    public SayCommand(Engine engine, String name, String text, boolean start, boolean finish, boolean left) {
        super();
        this.name = name;
        this.text = text;
        this.start = start;
        this.finish = finish;
        this.left = left;
        cutsceneScreen = (CutsceneScreen)engine.getScreen("cutscene");
    }

    public boolean act(float delta) {
        if (first) {
            first = false;
            cutsceneScreen.say(text, name, start, finish, left);
        } else {
            return cutsceneScreen.isSayFinished();
        }
        return false;
    }

}
