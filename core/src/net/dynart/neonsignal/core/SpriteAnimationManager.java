package net.dynart.neonsignal.core;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;

import java.util.HashMap;
import java.util.Map;

public class SpriteAnimationManager {

    private final Map<String, Animation> animations = new HashMap<String, Animation>();

    public void add(String name, Animation animation) {
        animations.put(name, animation);
    }

    public Animation get(String name) {
        return animations.get(name);
    }

    public void init(TextureManager textureManager, JsonValue resourcesJson) {
        JsonValue animations = resourcesJson.get("sprite_animations");
        for (JsonValue animation = animations.child(); animation != null; animation = animation.next()) {
            TextureAtlas atlas = textureManager.getAtlas(animation.getString("atlas"));
            String region = animation.getString("region");
            Array<TextureAtlas.AtlasRegion> regions = atlas.findRegions(region);
            float frameDuration = regions.size > 0 ? animation.getFloat("duration") / regions.size : 1;
            Animation.PlayMode mode = Animation.PlayMode.valueOf(animation.getString("mode", "normal").toUpperCase());
            Animation anim = new Animation(frameDuration, regions, mode);
            add(animation.name(), anim);
        }
    }

}
