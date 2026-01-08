package net.dynart.neonsignal.core;

import net.dynart.neonsignal.components.PlayerComponent;
import net.dynart.neonsignal.components.SecretComponent;
import net.dynart.neonsignal.core.listeners.MessageListener;

public class SecretManager {

    private final SoundManager soundManager;
    private final MessageListener foundListener;
    private final MessageListener fadeOutListener;
    private final MessageListener fadeInListener;
    private float alpha = 1.0f;
    private float alphaChange = 0;
    private GameScene gameScene;

    public SecretManager(Engine engine) {
        gameScene = engine.getGameScene();
        soundManager = engine.getSoundManager();
        foundListener = (sender, message) -> found();
        fadeInListener = (sender, message) -> fadeIn();
        fadeOutListener = (sender, message) -> fadeOut();
    }

    public float getAlpha() {
        return alpha;
    }

    public void addListeners(Entity entity) {
        MessageHandler messageHandler = entity.getMessageHandler();
        messageHandler.subscribe(SecretComponent.FOUND, foundListener);
        messageHandler.subscribe(SecretComponent.SHOW, fadeOutListener);
        messageHandler.subscribe(SecretComponent.HIDE, fadeInListener);
    }

    public void clear() {
        alpha = 1;
        alphaChange = 0;
    }

    public void update(float deltaTime) {
        alpha += alphaChange * deltaTime;
        if (alpha > 1) {
            alpha = 1;
            alphaChange = 0;
        }
        if (alpha < 0) {
            alpha = 0;
            alphaChange = 0;
        }
    }

    private void found() {
        PlayerComponent player = gameScene.getPlayer().getComponent(PlayerComponent.class);
        player.incSecretCount();
        soundManager.play("secret_found");
    }

    private void fadeOut() {
        alpha = 1.0f;
        alphaChange = -6f;
    }

    private void fadeIn() {
        alpha = 0.0f;
        alphaChange = 6f;
    }

}
