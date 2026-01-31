package net.dynart.neonsignal.screens;

import net.dynart.neonsignal.core.Engine;
import net.dynart.neonsignal.core.Screen;

public class GameFadeInScreen extends Screen {

    public GameFadeInScreen(Engine engine) {
        super(engine);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        Screen gameScreen = engine.getScreen("game");
        gameScreen.fadeIn();
        engine.moveToScreen("game");
    }
}
