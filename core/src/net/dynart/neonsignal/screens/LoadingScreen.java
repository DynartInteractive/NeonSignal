package net.dynart.neonsignal.screens;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import net.dynart.neonsignal.core.listeners.LoadingFinishedListener;
import net.dynart.neonsignal.core.Screen;
import net.dynart.neonsignal.core.Engine;
import net.dynart.neonsignal.ui.MenuBackground;

public class LoadingScreen extends Screen {

    private final AssetManager assetManager;

    private String nextScreen;
    private LoadingFinishedListener finishedListener;
    private final Label label;

    private final String[] texts = {"Loading.", "Loading..", "Loading..."};
    private int currentTextIndex = 0;
    private float currentDelta = 0;

    public LoadingScreen(Engine engine) {
        super(engine);
        assetManager = engine.getAssetManager();
        Label.LabelStyle ls = engine.getStyles().getDefaultLabelStyle();
        label = new Label(texts[currentTextIndex], ls);
        stage.addActor(label);
    }

    public void setNextScreen(String name) {
        nextScreen = name;
    }

    public void setFinishedListener(LoadingFinishedListener finishedListener) {
        this.finishedListener = finishedListener;
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        label.setPosition(stage.getWidth() - 250, 20);
        currentDelta += delta;
        if (currentDelta > 0.2) {
            currentDelta = 0;
            currentTextIndex++;
            if (currentTextIndex >= texts.length) {
                currentTextIndex = 0;
            }
        }
        label.setText(texts[currentTextIndex]);
        if (assetManager.update()) {
            if (finishedListener != null) {
                engine.loadingFinished();
                finishedListener.loadingFinished();
                finishedListener = null;
            }
            engine.moveToScreen(nextScreen);
            engine.resetDeltaTime();
        } else {
            float progress = assetManager.getProgress();
        }
    }

}
