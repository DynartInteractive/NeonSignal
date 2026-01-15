package net.dynart.neonsignal.core;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import net.dynart.neonsignal.components.ColliderComponent;
import net.dynart.neonsignal.components.ElectricSpikeComponent;
import net.dynart.neonsignal.components.PlatformComponent;
import net.dynart.neonsignal.components.PlayerComponent;

import java.util.List;
import java.util.Set;

import net.dynart.neonsignal.components.BodyComponent;
import net.dynart.neonsignal.components.EnemyComponent;
import net.dynart.neonsignal.components.VelocityComponent;
import net.dynart.neonsignal.components.ViewComponent;
import net.dynart.neonsignal.core.utils.AreaPosition;

public class GameScene {

    private final Engine engine;
    private final EngineConfig config;

    private final Vector2 startPosition = new Vector2();

    private TiledMap tiledMap;

    // gameplay related
    private final EntityFactory entityFactory;
    private EntityManager entityManager;
    private SecretManager secretManager;
    private Grid grid;
    private Entity player;
    private PathManager pathManager;
    private Camera camera;
    private CameraHandler cameraHandler;
    private ParticlePool particlePool;
    private BulletPool bulletPool;
    private BulletFactory bulletFactory;
    private ShapeRenderer shapeRenderer;
    private boolean firstFrame;

    // draw related
    private final int[] secretLayer = new int[1];
    private SpriteBatch batch;
    private Viewport viewport;
    private TiledMapRenderer tiledMapRenderer;
    private int[] frontLayers;
    private int[] backLayers;
    private Texture backgroundBackTexture;
    private Texture backgroundFrontTexture;

    private int secretCount;
    private int itemCount;
    private int enemyCount;

    public GameScene(Engine engine) {
        this.engine = engine;
        config = engine.getConfig();
        entityFactory = config.getEntityFactory();
    }

    public void init() {
        batch = new SpriteBatch();
        viewport = new ExtendViewport(
            config.getGameVirtualHeight() + 1, config.getGameVirtualHeight()//,
            //config.getGameMaxVirtualWidth(), config.getGameVirtualHeight()
        );
        camera = viewport.getCamera();
        shapeRenderer = new ShapeRenderer();
        grid = new Grid(engine);
        cameraHandler = new CameraHandler(engine);
        entityManager = new EntityManager();
        pathManager = new PathManager();
        secretManager = new SecretManager(engine);
        particlePool = new ParticlePool(engine);
        bulletPool = new BulletPool(engine);
        bulletFactory = new BulletFactory(engine);
        entityFactory.postConstruct(engine); // needs to be on the end
        firstFrame = true;
    }

    public Viewport getViewport() {
        return viewport;
    }

    public Grid getGrid() {
        return grid;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public Camera getCamera() {
        return camera;
    }

    public CameraHandler getCameraHandler() {
        return cameraHandler;
    }

    public ParticlePool getParticlePool() {
        return particlePool;
    }

    public BulletPool getBulletPool() {
        return bulletPool;
    }

    public BulletFactory getBulletFactory() {
        return bulletFactory;
    }

    public PathManager getPathManager() {
        return pathManager;
    }

    public EntityFactory getEntityFactory() {
        return entityFactory;
    }

    public SecretManager getSecretManager() {
        return secretManager;
    }

    public Entity getPlayer() {
        return player;
    }

    public Vector2 getStartPosition() {
        return startPosition;
    }

    public void setCounts(int enemies, int items, int secrets) {
        secretCount = secrets;
        enemyCount = enemies;
        itemCount = items;
    }

    public int getItemCount() {
        return itemCount;
    }

    public int getEnemyCount() {
        return enemyCount;
    }

    public int getSecretCount() {
        return secretCount;
    }

    public void clear() {
        setCounts(0, 0, 0);
        entityManager.clear();
        grid.clear();
        pathManager.clear();
        secretManager.clear();
        for (int i = 0; i < 4; i++) {
            cameraHandler.setNewLimit(i, -1);
        }
        firstFrame = true;
    }

    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    public void createTiledMapRenderer(TiledMap tiledMap) {
        this.tiledMap = tiledMap;
        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap, batch);
    }

    public TiledMap getTiledMap() {
        return tiledMap;
    }

    public void setPlayer(Entity player, PlayerAbility[] abilities) {
        this.player = player;
        BodyComponent body = player.getComponent(BodyComponent.class);
        startPosition.set(body.getCenterX(), body.getBottom());
        cameraHandler.setTarget(player);
        PlayerComponent playerComponent = player.getComponent(PlayerComponent.class);
        for (PlayerAbility ability : abilities) {
            playerComponent.addAbility(ability);
        }
    }

    public void setSecretLayer(int layerIndex) {
        secretLayer[0] = layerIndex;
    }

    public void setBackgroundBackTexture(Texture texture) {
        backgroundBackTexture = texture;
    }

    public void setBackgroundFrontTexture(Texture texture) {
        backgroundFrontTexture = texture;
    }

    public void createBackAndFrontLayers(List<Integer> backLayerList, List<Integer> frontLayerList) {
        backLayers = new int[backLayerList.size()];
        frontLayers = new int[frontLayerList.size()];
        for (int i = 0; i < backLayerList.size(); i++) {
            backLayers[i] = backLayerList.get(i);
        }
        for (int i = 0; i < frontLayerList.size(); i++) {
            frontLayers[i] = frontLayerList.get(i);
        }
    }

    public void update(float deltaTime) {
        float dt = firstFrame ? 0 : deltaTime;
        firstFrame = false;
        entityManager.update(dt);
        secretManager.update(dt);
        updateCamera(dt);
    }

    public void updateCamera(float deltaTime) {
        cameraHandler.update(deltaTime);
    }

    public void draw() {
        resetAlpha();
        animateItemLayers();
        tiledMapRenderer.setView((OrthographicCamera)camera);
        drawBackgrounds();
        tiledMapRenderer.render(backLayers);
        drawEntities(false);
        tiledMapRenderer.render(frontLayers);
        drawSecretLayer();
        drawEntities(true);
        drawDebugBodies();
    }

    private void animateItemLayers() {
        TiledMapTileLayer items1Layer = (TiledMapTileLayer)tiledMap.getLayers().get("Items1");
        TiledMapTileLayer items2Layer = (TiledMapTileLayer)tiledMap.getLayers().get("Items2");
        if (items1Layer != null && items2Layer != null) {
            float offsetY1 = (float) Math.sin(engine.getElapsedTime() * 10f) * 2f + 1f;
            float offsetY2 = (float) Math.cos(engine.getElapsedTime() * 10f) * 2f + 1f;
            items1Layer.setOffsetY(offsetY1);
            items2Layer.setOffsetY(offsetY2);
        }
    }

    public void dispose() {
        batch.dispose();
        if (backgroundFrontTexture != null) {
            backgroundFrontTexture.dispose();
        }
        if (backgroundBackTexture != null) {
            backgroundBackTexture.dispose();
        }
    }

    public void revive() {
        /*
        Vector2 startPosition = new Vector2(getStartPosition());
        Entity revive = ReviveComponent.getCurrent();
        if (revive != null) {
            BodyComponent body = revive.getComponent(BodyComponent.class);
            startPosition.set(body.getCenterX(), body.getBottom());
        }
        Entity playerEntity = getPlayer();
        PlayerComponent playerComponent = playerEntity.getComponent(PlayerComponent.class);
        playerComponent.revive(startPosition);
        List<Entity> emptyList = new ArrayList<>();
        List<Entity> oxygenList = entityManager.getListByClass(OxygenComponent.class);
        List<Entity> fallingList = entityManager.getListByClass(FallingComponent.class);
        List<Entity> movableList = entityManager.getListByClass(MovableComponent.class);
        for (Entity e : oxygenList == null ? emptyList : oxygenList) {
            OxygenComponent oxygen = e.getComponent(OxygenComponent.class);
            oxygen.reset();
        }
        for (Entity e : fallingList == null ? emptyList : fallingList) {
            FallingComponent falling = e.getComponent(FallingComponent.class);
            falling.reset();
        }
        for (Entity e : movableList == null ? emptyList : movableList) {
            MovableComponent mop = e.getComponent(MovableComponent.class);
            mop.reset();
            mop.setActive(mop.isStart());
        }
        */
    }

    private void drawSecretLayer() {
        batch.setColor(1f, 1f, 1f, secretManager.getAlpha());
        tiledMapRenderer.render(secretLayer);
        batch.setColor(1f, 1f, 1f, 1f);
    }

    private void drawBackgrounds() {
        batch.begin();
        batch.setProjectionMatrix(camera.combined);
        drawBackground(backgroundBackTexture, 10f, 0);
        drawBackground(backgroundFrontTexture, 2f, 0.1f);
        batch.end();
    }

    private void resetAlpha() {
        batch.setColor(1f, 1f, 1f, 1f);
    }

    private void drawEntities(boolean topLayer) {
        batch.begin();
        if (topLayer) {
            drawEntityLayer(EntityManager.TOP_LAYER);
        } else {
            for (int layer : entityManager.getLayers()) {
                if (layer != EntityManager.TOP_LAYER) { // do a diff or something? ...
                    drawEntityLayer(layer);
                }
            }
        }
        batch.end();
    }

    private void drawDebugBodies() {
        if (!engine.inDebugMode()) {
            return;
        }
        Set<Entity> colliders = entityManager.getAllByClassAndArea(ColliderComponent.class, player);
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        for (Entity entity : entityManager.getAllByClass(BodyComponent.class)) {

            Color col = entity.hasComponent(PlayerComponent.class)
                ? Color.GREEN
                : entity.hasComponent(EnemyComponent.class) || entity.hasComponent(ElectricSpikeComponent.class)
                    ? Color.RED
                    : Color.YELLOW;

            if (colliders.contains(entity)) {
                col = Color.ORANGE;
            }

            if (col == null) continue;

            shapeRenderer.setColor(col);

            BodyComponent body = entity.getComponent(BodyComponent.class);
            shapeRenderer.rect(body.getLeft(), body.getBottom(), body.getHalfWidth() * 2, body.getHalfHeight() * 2);
            if (entity.hasComponent(PlatformComponent.class)) {
                PlatformComponent platform = entity.getComponent(PlatformComponent.class);
                shapeRenderer.setColor(platform.getMountedEntities().isEmpty() ? Color.CYAN : Color.RED);
                shapeRenderer.rect(body.getLeft(), body.getTop(), body.getWidth(), 0);
            }
            if (entity.hasComponent(PlayerComponent.class)) {
                PlayerComponent player = entity.getComponent(PlayerComponent.class);
                ViewComponent view = entity.getComponent(ViewComponent.class);

                float dir = view.isFlipX() ? -1 : 1;
                Vector2 a = new Vector2(body.getCenterX(), body.getCenterY());
                Vector2 b = player.getCurrentGunBarrelPosition();

                Vector2 out = new Vector2();
                Vector2 gridOut = new Vector2();
                grid.getIntersection(a, b, gridOut);
                if (!entityManager.getIntersection(a, gridOut, out, entity)) {
                    out.set(gridOut);
                }

                shapeRenderer.setColor(Color.WHITE);
                shapeRenderer.line(a, out);
            }
        }
        shapeRenderer.end();
    }


    private void drawEntityLayer(int layer) {
        for (Entity entity : entityManager.getAllByLayer(layer)) {
            ViewComponent viewComponent = entity.getComponent(ViewComponent.class);
            viewComponent.draw(batch);
        }
    }

    private void drawBackground(Texture texture, float scrollRatio, float scrollY) {
        if (texture == null) {
            return;
        }
        float u = viewport.getWorldWidth() / texture.getWidth();
        float v = viewport.getWorldHeight() / texture.getHeight();
        float x = camera.position.x - viewport.getWorldWidth() / 2f;
        float y = camera.position.y - viewport.getWorldHeight() / 2f;
        float scrollX = x / (texture.getWidth() * scrollRatio);
        batch.draw(texture,
            x, y, viewport.getWorldWidth(), viewport.getWorldHeight(),
            scrollX, scrollY + v, scrollX + u, scrollY
        );
    }

}
