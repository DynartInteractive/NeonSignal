package net.dynart.neonsignal.components;

import com.badlogic.gdx.graphics.Color;

import net.dynart.neonsignal.core.Component;
import net.dynart.neonsignal.core.Entity;
import net.dynart.neonsignal.core.GameSprite;
import net.dynart.neonsignal.core.utils.Align;

public class MiniBarComponent extends Component {

    private static final Color DANGER_COLOR = new Color(1, 0, 0.45f, 1);
    private static final Color WARNING_COLOR = new Color(1, 0.88f, 0, 1);
    private static final Color GOOD_COLOR = new Color(0.5f, 0.88f, 0, 1);

    private final Color lineColor = new Color(0.5f, 0.9f, 1,1);
    private float visibleTime;
    private int viewIndex;

    private final boolean dynamicColor;

    public MiniBarComponent(boolean dynamicColor) {
        this.dynamicColor = dynamicColor;
    }

    public void add(Entity entity) {
        BodyComponent body = entity.getComponent(BodyComponent.class);
        ViewComponent view = entity.getComponent(ViewComponent.class);

        viewIndex = view.getLayerCount();

        GameSprite sprite = new GameSprite();
        sprite.setFlipEnabled(false);
        view.addSprite(sprite);
        view.setSprite(viewIndex, "mini_hp_bar_bg");
        sprite.setOffsetY(body.getHalfHeight() * 2f + 5f);

        sprite = new GameSprite();
        sprite.setFlipEnabled(false);
        view.addSprite(sprite);
        view.setSprite(viewIndex + 1, "mini_hp_line");
        sprite.setOffsetY(body.getHalfHeight() * 2f + 5f);
        sprite.setOffsetX(-sprite.getWidth() / 2f);
        sprite.setAlign(Align.LEFT_BOTTOM);
        sprite.setOrigin(0 ,0);

        sprite = new GameSprite();
        sprite.setFlipEnabled(false);
        view.addSprite(sprite);
        view.setSprite(viewIndex + 2, "mini_hp_bar");
        sprite.setOffsetY(body.getHalfHeight() * 2f + 5f);
    }

    public void setVisibleTime(float time) {
        visibleTime = time;
    }

    public void adjustDisplay(float value, float delta) {
        if (visibleTime > 0) {
            visibleTime -= delta;
            if (visibleTime < 0) {
                visibleTime = 0;
            }
        }
        ViewComponent view = entity.getComponent(ViewComponent.class);
        if (dynamicColor) {
            if (value < 0.5) {
                lineColor.set(DANGER_COLOR);
                lineColor.lerp(WARNING_COLOR, value * 2f);
            } else {
                lineColor.set(WARNING_COLOR);
                lineColor.lerp(GOOD_COLOR, (value - 0.5f) * 2f);
            }
        }

        float alpha = visibleTime < 0.5f ? visibleTime * 2f : 1f;

        Color c = view.getSprite(viewIndex).getColor();
        c.a = alpha;
        view.getSprite(viewIndex).setColor(c);

        lineColor.a = alpha;
        view.getSprite(viewIndex + 1).setColor(lineColor);
        view.getSprite(viewIndex + 1).setScale(value, 1);

        c = view.getSprite(viewIndex + 2).getColor();
        c.a = alpha;
        view.getSprite(viewIndex + 2).setColor(c);
    }

    public int getViewIndex() {
        return viewIndex;
    }

}
