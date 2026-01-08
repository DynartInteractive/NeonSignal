package net.dynart.neonsignal.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Align;

import net.dynart.neonsignal.core.controller.GameController;
import net.dynart.neonsignal.core.Screen;
import net.dynart.neonsignal.core.SoundManager;
import net.dynart.neonsignal.core.TextureManager;
import net.dynart.neonsignal.core.ui.FadeImage;
import net.dynart.neonsignal.core.Engine;

public class LogoScreen extends Screen {

    private static final Color DOT_COLOR = new Color(1, 1, 1, 1);
    private static final Color BIG_DOT_COLOR = new Color(1, 1, 1, 0.75f);

    private final SoundManager soundManager;
    private final GameController gameController;
    private final FadeImage textImage;
    private final Image bImage;
    private final Image yImage;
    private final Image gImage;
    private final Image rImage;
    private final FadeImage bBigImage;
    private final FadeImage yBigImage;
    private final FadeImage gBigImage;
    private final FadeImage rBigImage;
    private final Group group;

    private float startTime;

    public LogoScreen(Engine engine) {
        super(engine);
        gameController = engine.getGameController();
        soundManager = engine.getSoundManager();
        TextureManager textureManager = engine.getTextureManager();
        TextureAtlas atlas = textureManager.getAtlas("logo");

        group = new Group();
        stage.addActor(group);

        bImage = new Image(atlas.findRegion("blue"));
        bImage.setPosition(756, 368);
        bImage.setOrigin(Align.center);
        group.addActor(bImage);

        bBigImage = new FadeImage(atlas.findRegion("blue"));
        bBigImage.setPosition(756, 368);
        bBigImage.setOrigin(Align.center);
        group.addActor(bBigImage);

        yImage = new Image(atlas.findRegion("yellow"));
        yImage.setPosition(858, 355);
        yImage.setOrigin(Align.center);
        group.addActor(yImage);

        yBigImage = new FadeImage(atlas.findRegion("yellow"));
        yBigImage.setPosition(858, 355);
        yBigImage.setOrigin(Align.center);
        group.addActor(yBigImage);

        gImage = new Image(atlas.findRegion("green"));
        gImage.setPosition(834, 278);
        gImage.setOrigin(Align.center);
        group.addActor(gImage);

        gBigImage = new FadeImage(atlas.findRegion("green"));
        gBigImage.setPosition(834, 278);
        gBigImage.setOrigin(Align.center);
        group.addActor(gBigImage);

        rImage = new Image(atlas.findRegion("red"));
        rImage.setPosition(935, 250);
        rImage.setOrigin(Align.center);
        group.addActor(rImage);

        rBigImage = new FadeImage(atlas.findRegion("red"));
        rBigImage.setPosition(935, 250);
        rBigImage.setOrigin(Align.center);
        group.addActor(rBigImage);

        textImage = new FadeImage(atlas.findRegion("text"));
        textImage.setPosition(267, 286);
        group.addActor(textImage);
    }

    public void resize(int width, int height) {
        super.resize(width, height);
        group.setX((stage.getWidth() - 1280) / 2f);
        group.setY((stage.getHeight() - 720) / 2f);
        //Gdx.input.setCursorCatched(true);
    }

    @Override
    public void show() {
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException ignored) {}
        soundManager.playMusic("logo");
        startTime = engine.getElapsedTime();
        textImage.clearActions();
        textImage.getColor().a = 0;
        textImage.addAction(Actions.fadeIn(2f, Interpolation.pow2In));
        addDotAnimation(bImage, bBigImage, 1.5f);
        addDotAnimation(yImage, yBigImage, 3.05f);
        addDotAnimation(gImage, gBigImage, 3.12f);
        addDotAnimation(rImage, rBigImage, 4.1f);
        group.clearActions();
        group.addAction(Actions.sequence(
            Actions.delay(6.5f),
            Actions.fadeOut(3f, Interpolation.linear)
        ));
    }

    private void addDotAnimation(Actor actor, Actor bigActor, float delay) {
        actor.clearActions();
        actor.getColor().a = 0;
        actor.addAction(Actions.sequence(
            Actions.delay(delay),
            Actions.color(DOT_COLOR),
            Actions.scaleTo(1.3f, 1.3f, 0.07f, Interpolation.pow2In),
            Actions.scaleTo(1, 1, 0.07f, Interpolation.pow2Out)
        ));
        bigActor.clearActions();
        bigActor.getColor().a = 0;
        bigActor.addAction(Actions.sequence(
            Actions.delay(delay),
            Actions.color(BIG_DOT_COLOR),
            Actions.parallel(
                Actions.scaleTo(22f, 22f, 0.5f, Interpolation.pow2In),
                Actions.fadeOut(0.5f, Interpolation.pow2Out)
            )
        ));
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        float time = engine.getElapsedTime() - startTime;
        if (gameController.isAnyKeyPressed() || time > 11f) {
            engine.moveToScreen("menu");
        }
    }

}
