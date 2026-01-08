package net.dynart.neonsignal.components;

import net.dynart.neonsignal.core.Component;
import net.dynart.neonsignal.core.Entity;

public class ConveyorComponent extends Component {

    private final float speed;
    private final Entity mover;
    private int mounted;

    public ConveyorComponent(float speed, Entity mover) {
        super();
        this.speed = speed;
        this.mover = mover;
    }

    @Override
    public void postConstruct(final Entity entity) {
        super.postConstruct(entity);

        VelocityComponent moverVelocity = mover.getComponent(VelocityComponent.class);
        moverVelocity.setX(speed);

        messageHandler.subscribe(PlatformComponent.MOUNTED, (sender, message) -> {
            PlatformComponent platform = sender.getComponent(PlatformComponent.class);
            Entity mountedEntity = platform.getLastMountedEntity();
            mountedEntity.setParent(mover);
            mounted++;
        });

        messageHandler.subscribe(PlatformComponent.DISMOUNTED, (sender, message) -> mounted--);
    }

    @Override
    public void preUpdate(float delta) {
        if (mounted == 0) {
            BodyComponent body = entity.getComponent(BodyComponent.class);
            BodyComponent moverBody = mover.getComponent(BodyComponent.class);
            moverBody.setLeft(body.getLeft());
        }
    }

}
