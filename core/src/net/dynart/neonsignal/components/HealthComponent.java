package net.dynart.neonsignal.components;

import net.dynart.neonsignal.GameStage;
import net.dynart.neonsignal.core.Component;
import net.dynart.neonsignal.core.Entity;
import net.dynart.neonsignal.screens.GameScreen;

public class HealthComponent extends Component {


    public static final String INCREASED = "health_increased";
    public static final String DECREASED = "health_decreased";
    public static final String ZERO = "health_zero";

    private float value;
    private final float maxValue;
    private Entity decreaseByEntity;

    public HealthComponent(float maxValue) {
        this.value = maxValue;
        this.maxValue = maxValue;
    }

    public HealthComponent(float maxValue, float value) {
        this.value = value;
        this.maxValue = maxValue;
    }

    public Entity getDecreasedByEntity() {
        return decreaseByEntity;
    }

    public float getMaxValue() {
        return maxValue;
    }

    public float getValue() {
        return value;
    }

    public void revive() {
        value = maxValue;
        messageHandler.send(INCREASED);
    }

    public void increase(float v) {
        /*
        if (!active) {
            return;
        }
        */
        value += v;
        if (value > maxValue) {
            value = maxValue;
        }
        if (v != 0) {
            messageHandler.send(INCREASED);
        }
    }

    public void decrease(float v, Entity byEntity) {
        if (!active) {
            return;
        }
        decreaseByEntity = byEntity;
        value -= v;
        if (value <= 0) {
            value = 0;
        }
        if (v != 0) {
            messageHandler.send(DECREASED);
        }
        if (value == 0) {
            messageHandler.send(ZERO);
        }

        GameScreen gameScreen = (GameScreen) engine.getScreen("game");
        GameStage gameStage = (GameStage) gameScreen.getStage();
        BodyComponent body = entity.getComponent(BodyComponent.class);

        if (entity.hasComponent(PlayerComponent.class) || entity.hasComponent(EnemyComponent.class)) {
            String text = "-" + (int) (v * 100);
            gameStage.showItemScore(body.getCenterX(), body.getTop() + 8, text, 1, .44f, .44f);
        }

        if (value == 0 && entity.hasComponent(EnemyComponent.class)) {
            Entity player = engine.getGameScene().getPlayer();
            PlayerComponent playerComponent = player.getComponent(PlayerComponent.class);

            // TODO: entity.getType(), switch points

            int point = 100;
            playerComponent.addPoint(point);
            String text = "+" + point;
            gameStage.showItemScore(body.getCenterX(), body.getTop() + 16, text, 1, 0.8f, 0.1f);
        }
    }

    public void kill() {
        if (!active) {
            return;
        }
        value = 0;
        messageHandler.send(DECREASED);
        messageHandler.send(ZERO);

    }



}
