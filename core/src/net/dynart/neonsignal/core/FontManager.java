package net.dynart.neonsignal.core;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.JsonValue;

import java.util.HashMap;
import java.util.Map;

public class FontManager {

    private final AssetManager assetManager;
    private final Map<String, JsonValue> jsonValues = new HashMap<>();
    private final Map<String, BitmapFont> fonts = new HashMap<>();

    public FontManager(Engine engine) {
        assetManager = engine.getAssetManager();
    }

    public void load(JsonValue resourcesJson) {
        JsonValue fonts = resourcesJson.get("fonts");
        for (JsonValue fontJson = fonts.child(); fontJson != null; fontJson = fontJson.next()) {
            String path = fontJson.getString("path");
            boolean loadNow = fontJson.getBoolean("load_now", false);
            assetManager.load(path, BitmapFont.class);
            if (loadNow) {
                assetManager.finishLoading();
            }
            jsonValues.put(fontJson.name(), fontJson);
        }
    }

    public BitmapFont get(String name) {
        if (!jsonValues.containsKey(name)) { // not added
            throw new RuntimeException("Can't find font: " + name);
        }
        if (!fonts.containsKey(name)) { // not processed yet
            JsonValue fontJson = jsonValues.get(name);
            BitmapFont font = assetManager.get(fontJson.getString("path"), BitmapFont.class);
            TextureManager.setFilter(font.getRegion().getTexture(), fontJson);
            fonts.put(name, font);
        }
        return fonts.get(name);
    }

}
