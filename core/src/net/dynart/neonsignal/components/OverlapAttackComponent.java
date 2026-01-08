package net.dynart.neonsignal.components;

import net.dynart.neonsignal.core.Component;
import net.dynart.neonsignal.core.Entity;
import net.dynart.neonsignal.core.EntityManager;
import net.dynart.neonsignal.core.GameScene;

public class OverlapAttackComponent extends Component {

    public static final String ATTACK = "overlap_attack";

    private EntityManager entityManager;
    private float power;
    private BodyComponent body;
    private BodyComponent attackedBody;
    private BodyComponent exceptBody;
    private boolean enemy;

    public OverlapAttackComponent(float power, boolean enemy) {
        this.power = power;
        this.enemy = enemy;
    }

    public void setEnemy(boolean value) {
        enemy = value;
    }

    @Override
    public void postConstruct(final Entity entity) {
        super.postConstruct(entity);
        GameScene gameScene = engine.getGameScene();
        entityManager = gameScene.getEntityManager();
        body = entity.getComponent(BodyComponent.class);
    }

    public BodyComponent getAttackedBody() {
        return attackedBody;
    }

    public void setExceptBody(BodyComponent exceptBody) {
        this.exceptBody = exceptBody;
    }

    public void setPower(float power) {
        this.power = power;
    }

    @Override
    public void update(float delta) {
        for (Entity otherEntity : entityManager.getAllByClassAndArea(OverlapAttackableComponent.class, entity)) {
            OverlapAttackableComponent overlapAttackable = otherEntity.getComponent(OverlapAttackableComponent.class);
            if (overlapAttackable.isActive()) {
                processOverlap(otherEntity);
            }
        }
    }

    private void processOverlap(Entity otherEntity) {
        if (enemy && otherEntity.hasComponent(EnemyComponent.class)) {
            return;
        }
        BodyComponent otherBody = otherEntity.getComponent(BodyComponent.class);
        if (body == otherBody || exceptBody == otherBody || !body.isOverlap(otherBody)) {
            return;
        }
        HealthComponent health = otherEntity.getComponent(HealthComponent.class);
        if (health != null) {
            health.decrease(power, entity);
        }
        OverlapAttackableComponent attackable = otherEntity.getComponent(OverlapAttackableComponent.class);
        attackable.attacked(entity);
        attackedBody = otherBody;
        messageHandler.send(ATTACK);

    }

}
