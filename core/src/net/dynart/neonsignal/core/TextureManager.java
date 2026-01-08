package net.dynart.neonsignal.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.JsonValue;

import java.util.HashMap;
import java.util.Map;

import net.dynart.neonsignal.core.utils.StringUtil;

public class TextureManager {

    private final AssetManager assetManager;
    private final Map<String, String> paths = new HashMap<String, String>();
    private final Map<String, Skin> skins = new HashMap<String, Skin>();

    public TextureManager(Engine engine) {
        assetManager = engine.getAssetManager();
    }

    public void add(String name, String path) {
        paths.put(name, path);
    }

    private void checkName(String name) {
        if (!paths.containsKey(name)) {
            throw new RuntimeException("Can't find texture or atlas: " + name);
        }
    }

    public TextureAtlas getAtlas(String name) {
        checkName(name);
        return assetManager.get(paths.get(name), TextureAtlas.class);
    }

    public Texture getTexture(String name) {
        checkName(name);
        return assetManager.get(paths.get(name), Texture.class);
    }

    public void load(JsonValue resourcesJson) {
        loadAtlases(resourcesJson);
        loadTextures(resourcesJson);
    }

    private void loadTextures(JsonValue resourcesJson) {
        JsonValue textures = resourcesJson.get("textures");
        for (JsonValue texture = textures.child(); texture != null; texture = texture.next()) {
            String path = texture.getString("path");
            assetManager.load(path, Texture.class);
            add(texture.name(), path);
        }
    }

    private void loadAtlases(JsonValue resourcesJson) {
        JsonValue atlases = resourcesJson.get("atlases");
        for (JsonValue atlas = atlases.child(); atlas != null; atlas = atlas.next()) {
            String path = atlas.getString("path");
            boolean loadNow = atlas.getBoolean("load_now", false);
            assetManager.load(path, TextureAtlas.class);
            if (loadNow) {
                assetManager.finishLoading();
            }
            add(atlas.name(), path);
        }
    }

    public void init(JsonValue resourcesJson) {
        JsonValue textures = resourcesJson.get("textures");
        for (JsonValue textureJson = textures.child(); textureJson != null; textureJson = textureJson.next()) {
            Texture texture = getTexture(textureJson.name());
            setFilter(texture, textureJson);
            setWrap(texture, textureJson);
        }
        JsonValue atlases = resourcesJson.get("atlases");
        for (JsonValue atlasJson = atlases.child(); atlasJson != null; atlasJson = atlasJson.next()) {
            TextureAtlas a = getAtlas(atlasJson.name());
            for (Texture texture : a.getTextures()) {
                setFilter(texture, atlasJson);
            }
        }
    }

    public static void setFilter(Texture texture, JsonValue textureJson) {
        String filterName = StringUtil.camelize(textureJson.getString("filter", "linear"));
        Gdx.app.debug("TextureManager", "Set `" + filterName + "` filter for " + textureJson.get("path"));
        Texture.TextureFilter filter = Texture.TextureFilter.valueOf(filterName);
        texture.setFilter(filter, filter);
    }

    private void setWrap(Texture texture, JsonValue jsonTexture) {
        String wrapName = StringUtil.camelize(jsonTexture.getString("wrap", "repeat"));
        Texture.TextureWrap wrap = Texture.TextureWrap.valueOf(wrapName);
        texture.setWrap(wrap, wrap);
    }

    public Skin getSkin(String name) {
        if (skins.containsKey(name)) {
            return skins.get(name);
        }
        TextureAtlas atlas = getAtlas(name);
        Skin skin = new Skin(atlas);
        skins.put(name, skin);
        return skin;
    }

}
