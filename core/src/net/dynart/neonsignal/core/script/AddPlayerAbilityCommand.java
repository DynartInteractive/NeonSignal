package net.dynart.neonsignal.core.script;

import com.badlogic.gdx.Gdx;

import net.dynart.neonsignal.components.PlayerComponent;
import net.dynart.neonsignal.core.Engine;
import net.dynart.neonsignal.core.PlayerAbility;
import net.dynart.neonsignal.screens.GameScreen;

public class AddPlayerAbilityCommand implements Command {

    private final Engine engine;
    private final PlayerAbility ability;

    public AddPlayerAbilityCommand(Engine engine, PlayerAbility ability) {
        this.engine = engine;
        this.ability = ability;
    }

    @Override
    public boolean act(float delta) {
        // add ability
        PlayerComponent player = engine.getGameScene().getPlayer().getComponent(PlayerComponent.class);
        player.addAbility(ability);
        // refresh hud
        GameScreen gameScreen = (GameScreen)engine.getScreen("game");
        gameScreen.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        return true;
    }
}
