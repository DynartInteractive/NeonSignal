package net.dynart.neonsignal.components;

import net.dynart.neonsignal.core.Component;
import net.dynart.neonsignal.core.Entity;
import net.dynart.neonsignal.core.GameSprite;
import net.dynart.neonsignal.core.listeners.MessageListener;

public class RushComponent extends Component {

    private BodyComponent body;
    private BodyComponent watchBody;
    private ViewComponent view;
    private VelocityComponent velocity;
    private EnemyComponent enemy;
    private WalkerComponent walker;
    private float rushTime;
    private float originalSpeed;
    private float speed = 80f; // TODO: rush_speed param
    private float rotation = 0; // TODO: rush_rotation param
    private int spriteLayer;

    @Override
    public void postConstruct(final Entity entity) {
        super.postConstruct(entity);

        // TODO: better solution for "Watcher"
        watchBody = new BodyComponent(16*5, 16);
        watchBody.postConstruct(entity);
        //

        body = entity.getComponent(BodyComponent.class);
        velocity = entity.getComponent(VelocityComponent.class);
        enemy = entity.getComponent(EnemyComponent.class);
        walker = entity.getComponent(WalkerComponent.class);
        view = entity.getComponent(ViewComponent.class);

        spriteLayer = view.getSpriteCount();
        view.addSprite(new GameSprite());
        view.setSprite(spriteLayer, enemy.getSpritePrefix() + "_rush_rotate");
        view.getSprite(spriteLayer).setVisible(false);

        originalSpeed = walker.getSpeed();

        messageHandler.subscribe(EnemyComponent.DAMAGED, (sender, message) -> endRush());
    }

    @Override
    public void update(float delta) {

        if (rushTime > 0) {
            rushTime -= delta;
            if (rushTime < 0) {
                endRush();
            } else if (rushTime < 4.7f) { // rush_time - anim duration (.3)
                walker.setSpeed(speed);
                view.getSprite(0).setVisible(false);
                view.getSprite(spriteLayer).setVisible(true);
                rotation += delta * 720f;
                view.setRotation(spriteLayer, velocity.getX() > 0 ? -rotation : rotation);
            }
            return;
        }

        // can we see the player?
        watchBody.setY(body.getGlobalY());
        if (velocity.getX() < 0) {
            watchBody.setRight(body.getLeft());
        } else {
            watchBody.setLeft(body.getRight());
        }
        Entity player = watchBody.overlapOther(PlayerComponent.class);
        if (player != null) { // if yes..
            // rush start
            view.setAnimationTime(0, 0);
            view.setAnimation(0, enemy.getSpritePrefix() + "_rush");
            rushTime = 5f; // TODO: rush_time param
        }

    }

    private void endRush() {
        // rush done
        rushTime = 0;
        walker.setSpeed(originalSpeed);
        view.setAnimation(0, enemy.getSpritePrefix());
        view.getSprite(0).setVisible(true);
        view.getSprite(spriteLayer).setVisible(false);
    }
}
