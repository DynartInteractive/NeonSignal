package net.dynart.neonsignal.core.ui;

import com.badlogic.gdx.scenes.scene2d.Action;

import net.dynart.neonsignal.core.Engine;

public class FadeToAction extends Action {

    private final String screenName;
    private final Engine engine;

    public FadeToAction(Engine engine, String screenName) {
        this.engine = engine;
        this.screenName = screenName;
    }

    @Override
    public boolean act(float delta) {
        engine.getScreen(screenName).fadeIn();
        engine.setScreen(screenName);
        return true;
    }
}
