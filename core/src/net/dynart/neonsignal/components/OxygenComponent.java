package net.dynart.neonsignal.components;

import net.dynart.neonsignal.core.Component;
import net.dynart.neonsignal.core.Entity;

public class OxygenComponent extends Component {

    public static final String[] sounds = { "oxygen1", "oxygen2" };

    private BodyComponent body;
    private ViewComponent view;
    private float inactiveTime;
    private boolean growing;
    private boolean using;

    @Override
    public void postConstruct(final Entity entity) {
        super.postConstruct(entity);
        body = entity.getComponent(BodyComponent.class);
        view = entity.getComponent(ViewComponent.class);
    }

    public void update(float delta) {
        if (inactiveTime > 0) {
            inactiveTime -= delta;
            if (inactiveTime < 4.75f && using) {
                view.setVisible(false);
                using = false;
            }
            if (inactiveTime < 0.4f && !growing) {
                growing = true;
                view.setVisible(true);
                view.setAnimationTime(0, 0);
                view.setAnimation(0, "oxygen_grow");
            }
            if (inactiveTime < 0) {
                reset();
            }
            return;
        }
        Entity e = body.overlapOther(PlayerComponent.class);
        if (e != null) {
            PlayerComponent player = e.getComponent(PlayerComponent.class);
            player.incOxygen();
            engine.getSoundManager().playRandom(sounds);
            inactiveTime = 5f;
            view.setAnimationTime(0, 0);
            view.setAnimation(0, "oxygen_use");
            using = true;
        }
    }

    public void reset() {
        growing = false;
        view.setAnimationTime(0, 0);
        view.setAnimation(0, "oxygen_idle");
        view.setVisible(true);
        inactiveTime = 0;
    }
}
