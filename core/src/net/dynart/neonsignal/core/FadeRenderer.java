package net.dynart.neonsignal.core;

import com.badlogic.gdx.scenes.scene2d.Stage;

public interface FadeRenderer {
    void init(Engine engine);
    void draw(Stage stage, float value);
    void dispose();
}
