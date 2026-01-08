package net.dynart.neonsignal.components;

import net.dynart.neonsignal.core.Component;
import net.dynart.neonsignal.core.Entity;
import net.dynart.neonsignal.core.EntityManager;
import net.dynart.neonsignal.core.GameScene;
import net.dynart.neonsignal.core.utils.Parameters;
import net.dynart.neonsignal.core.EntityFactory;

public class BoxComponent extends Component {

    private CrushComponent crushComponent;
    private EntityFactory entityFactory;
    private EntityManager entityManager;
    private final String itemName;
    private Parameters parameters;
    private BodyComponent body;
    private boolean resetHorizontalVelocity;

    public BoxComponent(String itemName) {
        this.itemName = itemName;
    }

    public String getItemName() {
        return itemName;
    }

    @Override
    public void postConstruct(final Entity entity) {
        super.postConstruct(entity);
        GameScene gameScene = engine.getGameScene();
        entityFactory = gameScene.getEntityFactory();
        entityManager = gameScene.getEntityManager();
        body = entity.getComponent(BodyComponent.class);
        crushComponent = entity.getComponent(CrushComponent.class);
        parameters = new Parameters();

        messageHandler.subscribe(OverlapAttackableComponent.ATTACKED, (sender, message) -> {
            crushComponent.setActive(true);
            spawnBonus();
        });
    }

    private void spawnBonus() {
        if (itemName != null && !itemName.isEmpty()) {
            parameters.set("type", itemName);
            parameters.set("x", body.getLeft());
            parameters.set("y", body.getBottom());
            parameters.set("width", 16);
            parameters.set("height", 16);
            entityManager.add(entityFactory.create(parameters));
        }
    }
}
