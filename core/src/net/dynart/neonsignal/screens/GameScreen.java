package net.dynart.neonsignal.screens;

import net.dynart.lisa.components.BodyComponent;
import net.dynart.lisa.core.Engine;
import net.dynart.lisa.core.Entity;
import net.dynart.lisa.core.Level;

import net.dynart.neonsignal.GameStage;

import java.util.HashMap;
import java.util.Map;

public class GameScreen extends net.dynart.lisa.screens.GameScreen {

    public GameScreen(final Engine engine) {
        super(engine);
    }

    @Override
    protected net.dynart.lisa.screens.GameStage createGameStage() {
        return new GameStage(viewport, engine, this);
    }

    @Override
    protected void onLevelStarted(Level level) {
        Map<String, Object> params = new HashMap<>();
        params.put("level_name", level.getName());
        engine.getAnalyticsManager().track("level_start", params);
    }

    @Override
    protected void onPlayerDeath() {
        Level level = getCurrentLevel();
        if (level == null) {
            return;
        }
        Entity player = gameScene.getPlayer();
        BodyComponent body = player.getComponent(BodyComponent.class);
        Map<String, Object> params = new HashMap<>();
        params.put("level_name", level.getName());
        params.put("death_x", (int) body.getCenterX());
        params.put("death_y", (int) body.getBottom());
        engine.getAnalyticsManager().track("player_death", params);
    }

}
