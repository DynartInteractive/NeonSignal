package net.dynart.neonsignal.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.PolylineMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.maps.tiled.TiledMapTileSets;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject;
import com.badlogic.gdx.math.Polyline;

import net.dynart.neonsignal.components.BoxComponent;
import net.dynart.neonsignal.components.EnemyComponent;
import net.dynart.neonsignal.components.ItemComponent;
import net.dynart.neonsignal.components.ReviveComponent;
import net.dynart.neonsignal.components.SecretComponent;
import net.dynart.neonsignal.core.utils.Parameters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class GameSceneLoader {

    public static final String LOG_TAG = "GameSceneLoader";

    private enum LoadType {
        FIRST, SECOND, LAST
    }

    private final List<Integer> frontLayerList = new ArrayList<Integer>();
    private final List<Integer> backLayerList = new ArrayList<Integer>();
    private final Parameters parameters = new Parameters();
    private final GameScene gameScene;
    private final Grid grid;
    private final TextureManager textureManager;
    private final EntityFactory entityFactory;
    private final EntityManager entityManager;
    private final SecretManager secretManager;
    private final PathManager pathManager;
    private final SoundManager soundManager;

    private List<Integer> currentLayerList;
    private boolean gridSizeSet;
    private Entity player;
    private TiledMap tiledMap;

    Map<Integer, Parameters> defaultParametersMap = new HashMap<Integer, Parameters>();

    public GameSceneLoader(Engine engine) {
        textureManager = engine.getTextureManager();
        gameScene = engine.getGameScene();
        grid = gameScene.getGrid();
        entityFactory = gameScene.getEntityFactory();
        entityManager = gameScene.getEntityManager();
        secretManager = gameScene.getSecretManager();
        pathManager = gameScene.getPathManager();
        soundManager = engine.getSoundManager();
    }

    public void loadLevel(String levelPath) {
        reset();
        loadTiledMap(levelPath);
        loadDefaultParameters();
        loadMusic();
        loadBackground();
        loadLayers();
        checkPlayerPresence();
        calculateCounts();
        setPlayer();
        gameScene.createBackAndFrontLayers(backLayerList, frontLayerList);
        gameScene.createTiledMapRenderer(tiledMap);
        ReviveComponent.clearCurrent();
    }

    private void loadMusic() {
        String musicName = "beach"; // default
        MapProperties properties = tiledMap.getProperties();
        if (properties.containsKey("music")) {
            musicName = properties.get("music").toString();
        }
        soundManager.playMusic(musicName);
    }

    private void setPlayer() {

        // set player abilities via the "player_abilities" property
        PlayerAbility[] abilities;
        MapProperties properties = tiledMap.getProperties();
        if (properties.containsKey("player_abilities")) {
            List<PlayerAbility> list = new LinkedList<>();
            for (String abilityString : properties.get("player_abilities").toString().split(",")) {
                abilityString = abilityString.trim().toUpperCase();
                list.add(PlayerAbility.valueOf(abilityString));
            }
            abilities = new PlayerAbility[list.size()];
            list.toArray(abilities);
        } else {
            // .. or if it doesn't exist, add all abilities
            abilities = PlayerAbility.values();
        }

        // set the player with abilities
        gameScene.setPlayer(player, abilities);
    }

    private void calculateCounts() {

        // count enemies
        List<Entity> enemyList = entityManager.getAllByClass(EnemyComponent.class);
        int enemies = enemyList == null ? 0 : enemyList.size();

        // count secrets
        List<Entity> secretList = entityManager.getAllByClass(SecretComponent.class);
        int secrets = secretList == null ? 0 : secretList.size();

        // count items (entity)
        List<Entity> itemList = entityManager.getAllByClass(ItemComponent.class);
        List<Entity> boxList = entityManager.getAllByClass(BoxComponent.class);
        int items = itemList == null ? 0 : itemList.size();
        if (boxList != null) {
            for (Entity e : boxList) {
                BoxComponent box = e.getComponent(BoxComponent.class);
                if (box.getItemName() != null && !box.getItemName().equals("")) {
                    items++;
                }
            }
        }

        // count items (tile)
        TiledMapTileLayer itemLayer = (TiledMapTileLayer)tiledMap.getLayers().get("Items1");
        if (itemLayer != null) {
            for (int j = 0; j < grid.getHeight(); j++) {
                for (int i = 0; i < grid.getWidth(); i++) {
                    if (itemLayer.getCell(i, j) != null) {
                        items++;
                    }
                }
            }
        }

        // set counts
        gameScene.setCounts(enemies, items, secrets);
    }

    private void checkPlayerPresence() {
        if (player == null) {
            throw new RuntimeException("Player not found");
        }
    }

    private void loadTiledMap(String levelPath) {
        TmxMapLoader tmxMapLoader = new TmxMapLoader();
        if (tiledMap != null) {
            tiledMap.dispose();
        }
        FileHandle fileHandle = Gdx.files.internal(levelPath);
        if (!fileHandle.exists()) {
            throw new RuntimeException("File not found for load: " + levelPath);
        }
        tiledMap = tmxMapLoader.load(levelPath);
    }

    private void reset() {
        gameScene.clear();
        frontLayerList.clear();
        backLayerList.clear();
        parameters.clear();
        gridSizeSet = false;
        currentLayerList = backLayerList;
        player = null;
    }

    private void loadLayers() {
        int layerIndex = 0;
        for (MapLayer mapLayer : tiledMap.getLayers()) {
            loadLayer(layerIndex, mapLayer);
            layerIndex++;
        }
    }

    private void loadBackground() {
        MapProperties properties = tiledMap.getProperties();
        gameScene.setBackgroundBackTexture(loadTextureFromProperty("background_back", properties));
        gameScene.setBackgroundFrontTexture(loadTextureFromProperty("background_front", properties));
    }

    private void loadDefaultParameters() {
        defaultParametersMap.clear();
        TiledMapTileSets tileSets = tiledMap.getTileSets();
        TiledMapTileSet objectsTileSet = tileSets.getTileSet("objects");
        if (objectsTileSet == null) {
            return;
        }
        for (TiledMapTile tile : objectsTileSet) {
            MapProperties properties = tile.getProperties();
            int id = tile.getId();
            Parameters parameters = new Parameters();
            setParametersFromMapProperties(properties, parameters);
            defaultParametersMap.put(id, parameters);
            Gdx.app.log(LOG_TAG, "Default parameter added for GID " + id);
            for (String k : parameters.getKeySet()) {
                Gdx.app.log(LOG_TAG, "  " + k + ": " + parameters.get(k));
            }
        }
    }

    private Texture loadTextureFromProperty(String key, MapProperties properties) {
        Texture texture = null;
        if (properties.containsKey(key)) {
            String name = properties.get(key).toString();
            texture = textureManager.getTexture(name);
        }
        return texture;
    }

    private void loadLayer(int layerIndex, MapLayer mapLayer) {
        MapObjects mapObjects = mapLayer.getObjects();
        // if the layer has objects then load those
        if (mapObjects.getCount() > 0) {
            currentLayerList = frontLayerList;
            // load the paths first
            for (MapObject mapObject : mapObjects) {
                loadObject(mapObject, LoadType.FIRST);
            }
            // .. then the things that needs to be at the beginning of the entity lists
            // (platform, movable, etc.)
            for (MapObject mapObject : mapObjects) {
                loadObject(mapObject, LoadType.SECOND);
            }
            // .. then the rest of the objects
            for (MapObject mapObject : mapObjects) {
                loadObject(mapObject, LoadType.LAST);
            }
        }
        // .. else load the grid, secret or item layer
        else if (mapLayer.getClass() == TiledMapTileLayer.class) {
            TiledMapTileLayer tiledMapLayer = (TiledMapTileLayer) mapLayer;
            setGridSize(tiledMapLayer);
            MapProperties properties = tiledMapLayer.getProperties();
            if (properties.containsKey("block")) {
                // grid
                loadGrid(tiledMapLayer);
            } else if (properties.containsKey("secret")) {
                // secret layer
                gameScene.setSecretLayer(layerIndex);
            } else {
                // items: init the two layers for them
                if (tiledMapLayer.getName().equals("Items1")) {
                    TiledMapTileLayer tiledMapLayer2 = (TiledMapTileLayer)tiledMap.getLayers().get("Items2");
                    if (tiledMapLayer2 == null) {
                        throw new RuntimeException("Items2 layer doesn't exist!");
                    }
                    for (int j = 0; j < grid.getHeight(); j++) {
                        for (int i = 0; i < grid.getWidth(); i++) {
                            if (i % 2 == 1) {
                                tiledMapLayer2.setCell(i, j, tiledMapLayer.getCell(i, j));
                                tiledMapLayer.setCell(i, j, null);
                            }
                        }
                    }
                }
                currentLayerList.add(layerIndex);
            }
        }
    }

    private void loadGrid(TiledMapTileLayer tiledMapLayer) {
        for (int y = 0; y < tiledMapLayer.getHeight(); y++) {
            for (int x = 0; x < tiledMapLayer.getWidth(); x++) {
                TiledMapTileLayer.Cell cell = tiledMapLayer.getCell(x, y);
                if (cell == null) {
                    continue;
                }
                TiledMapTile tile = cell.getTile();
                MapProperties mapProps = tile.getProperties();
                grid.set(Grid.Layer.BLOCK, x, y, mapProps.containsKey("block") || mapProps.containsKey("slider"));
                grid.set(Grid.Layer.TOP_BLOCK, x, y, mapProps.containsKey("top_block") || mapProps.containsKey("top_slider"));
                grid.set(Grid.Layer.SLIDER, x, y, mapProps.containsKey("slider") || mapProps.containsKey("top_slider"));
                grid.set(Grid.Layer.WATER, x, y, mapProps.containsKey("water"));
                grid.set(Grid.Layer.SPIKE, x, y, mapProps.containsKey("spike"));
                grid.set(Grid.Layer.POISON, x, y, mapProps.containsKey("poison"));
                grid.set(Grid.Layer.DEATH, x, y, mapProps.containsKey("death"));
            }
        }
    }

    private void setGridSize(TiledMapTileLayer tiledMapLayer) {
        if (!gridSizeSet) {
            grid.setWidth(tiledMapLayer.getWidth());
            grid.setHeight(tiledMapLayer.getHeight());
            gridSizeSet = true;
        }
    }

    private void loadObject(MapObject mapObject, LoadType loadType) {
        boolean isPolyline = mapObject.getClass() == PolylineMapObject.class;
        boolean isEntity = mapObject.getClass() == TiledMapTileMapObject.class || mapObject.getClass() == RectangleMapObject.class;
        if (loadType == LoadType.FIRST && isPolyline) {
            loadPath((PolylineMapObject)mapObject);
        } else if (loadType != LoadType.FIRST && isEntity) {
            loadEntity(mapObject, loadType);
        }
    }

    private void loadEntity(MapObject object, LoadType loadType) {
        Entity entity = createEntity(object, loadType);
        if (entity == null) {
            return;
        }
        String type = parameters.get("type");
        if (type.equals("player")) {
            player = entity;
        } else if (type.equals("secret")) {
            secretManager.addListeners(entity);
        }
        entityManager.addToLists(entity);
    }

    private void setParametersFromMapProperties(MapProperties properties, Parameters parameters) {
        Iterator<String> keys = properties.getKeys();
        while (keys.hasNext()) {
            String key = keys.next();
            parameters.set(key, properties.get(key).toString());
        }
    }

    private Entity createEntity(MapObject object, LoadType loadType) {
        MapProperties properties = object.getProperties();
        parameters.clear();

        // load default values for the parameters
        if (properties.containsKey("gid")) {
            int gid = Integer.parseInt(properties.get("gid").toString());
            if (defaultParametersMap.containsKey(gid)) {
                Parameters defaultParameters = defaultParametersMap.get(gid);
                parameters.copy(defaultParameters);
            }
        }

        // set name, type and any other parameters
        parameters.set("name", object.getName());
        setParametersFromMapProperties(properties, parameters);
        String type = parameters.get("type");
        if (type == null) {
            return null;
        }

        // check the current load type and create the entity only if needed
        boolean isSecond = type.equals("platform")
            || type.equals("falling_platform")
            || type.equals("tram")
            || type.equals("conveyor")
            || type.equals("springboard")
            || type.equals("button")
            || type.equals("movable");
        if ((loadType == LoadType.SECOND && !isSecond) || (loadType == LoadType.LAST && isSecond)) {
            return null;
        }

        return entityFactory.create(parameters);
    }

    private void loadPath(PolylineMapObject object) {
        Polyline polyline = object.getPolyline();
        Path path = new Path(polyline.getTransformedVertices());
        pathManager.add(object.getName(), path);
    }

    public void dispose() {
        if (tiledMap != null) {
            tiledMap.dispose();
        }
    }

}
