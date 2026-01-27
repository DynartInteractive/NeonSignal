package net.dynart.neonsignal.components;

import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Vector2;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.dynart.neonsignal.GameStage;
import net.dynart.neonsignal.core.BulletOptions;
import net.dynart.neonsignal.core.EntityManager;
import net.dynart.neonsignal.core.BulletFactory;
import net.dynart.neonsignal.core.Component;
import net.dynart.neonsignal.core.Entity;
import net.dynart.neonsignal.core.GameScene;
import net.dynart.neonsignal.core.Grid;
import net.dynart.neonsignal.core.ParticlePool;
import net.dynart.neonsignal.core.PlayerAbility;
import net.dynart.neonsignal.core.SoundManager;
import net.dynart.neonsignal.core.controller.GameController;
import net.dynart.neonsignal.core.listeners.MessageListener;
import net.dynart.neonsignal.core.utils.Align;
import net.dynart.neonsignal.core.utils.Direction;
import net.dynart.neonsignal.screens.GameScreen;

public class PlayerComponent extends Component {

    public enum GunPosition {

        UP("up"),
        DIAGONAL_UP("diagonal_up"),
        FORWARD(null),
        DIAGONAL_DOWN("diagonal_down");

        private final String name;

        GunPosition(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

    }

    public static final String SCORE_CHANGED = "player_score_changed";
    public static final String WANTS_TO_DROP = "player_wants_to_drop";

    private static final float maxUnderWaterTime = 10f;

    private GameController gameController;
    private VelocityComponent velocity;
    private HealthComponent health;
    private MiniBarComponent miniBar;
    private BodyComponent body;
    private WaterCollisionComponent waterCollision;
    private EntityManager entityManager;
    private SoundManager soundManager;
    private Grid grid;
    private BulletFactory bulletFactory;
    private ParticlePool particlePool;

    private int score;

    private int itemCount;
    private int knockoutCount;
    private int secretCount;

    private boolean jumpReleased = true;
    private int jumpCounter = 0;
    private float inAirTime = 0;
    private float invincibleTime;
    private boolean lastXDown = false;

    private boolean flipX;
    private int axisXSign;
    private float axisXSignTime;

    private boolean jumpOnNextFrame;
    private float horizontalSign = 0;
    private boolean lastHorizontalDown = false;
    private float acceleration;
    private boolean sliding = false;
    private boolean inPain;
    private boolean wasInPain;
    private float inPainTime;
    private float invincibleBlinkTime;
    private float nextHorizontalVelocityForPain;

    private boolean changeDown = false;
    private boolean lastChangeDown = false;
    private int currentWeaponIndex;

    private float attackTime;
    private float lastAttackTime;
    private String currentAnimationName = "player_idle";
    //private GameSprite weaponSprite;

    private static final float CROUCH_HEIGHT = 128f;
    private static final float HEIGHT = 190f;


    private Entity pushedBy = null;
    private boolean headUnderWater;
    private boolean lastHeadUnderWater;
    private boolean footUnderWater;
    private boolean lastFootUnderWater;
    private float underWaterTime;
    private boolean oxygenDamage;
    private float poisonedTime;
    private int lastPoisonedSecond;
    private boolean poisonDamage;
    private Entity onSwitch;

    private final Set<PlayerAbility> abilities = new HashSet<>();


    private static final String[] fallDownSounds = { "falldown", "falldown2", "falldown3" };
    private static final String[] punchSounds = { "punch1", "punch2", "punch3" }; // 577063__bertsz__cinematic-punches.wav
    private static final String[] swimSounds = { "swim1", "swim2", "swim3" }; // avosound.com, Sound ID: 80475, Filename: Water,Churn,Heavy,Underwater,Metallic.wav

    private String animPrefix = "";

    private GunPosition gunPosition = GunPosition.FORWARD;
    private Map<String, Vector2> gunBarrelPosition = new HashMap<>();
    private Map<String, Vector2> gunBarrelVelocity = new HashMap<>();

    @Override
    public void postConstruct(final Entity entity) {
        super.postConstruct(entity);
        ViewComponent view = entity.getComponent(ViewComponent.class);
        view.setLayer(90);

        gunBarrelPosition.put("player_idle_up", new Vector2(-35, 281));
        gunBarrelPosition.put("player_idle_diagonal_up", new Vector2(50, 260));
        gunBarrelPosition.put("player_idle", new Vector2(84, 123));
        gunBarrelPosition.put("player_idle_diagonal_down", new Vector2(97, 60));
        gunBarrelPosition.put("player_run_diagonal_up", new Vector2(40, 266));
        gunBarrelPosition.put("player_run", new Vector2(92, 144));
        gunBarrelPosition.put("player_run_diagonal_down", new Vector2(74, 69));
        gunBarrelPosition.put("player_jump_begin_up", new Vector2(-35, 294));
        gunBarrelPosition.put("player_jump_end_up", new Vector2(-35, 294));
        gunBarrelPosition.put("player_jump_begin_diagonal_up", new Vector2(52, 275));
        gunBarrelPosition.put("player_jump_end_diagonal_up", new Vector2(52, 275));
        gunBarrelPosition.put("player_jump_begin", new Vector2(106, 160));
        gunBarrelPosition.put("player_jump_end", new Vector2(106, 160));
        gunBarrelPosition.put("player_jump_begin_diagonal_down", new Vector2(79, 77));
        gunBarrelPosition.put("player_jump_end_diagonal_down", new Vector2(79, 77));

        gunBarrelVelocity.put("player_idle_up", new Vector2(0, 1));
        gunBarrelVelocity.put("player_idle_diagonal_up", new Vector2(0.707f, 0.707f));
        gunBarrelVelocity.put("player_idle", new Vector2(1, 0));
        gunBarrelVelocity.put("player_idle_diagonal_down", new Vector2(0.707f, -0.707f));
        gunBarrelVelocity.put("player_run_diagonal_up", new Vector2(0.707f, 0.707f));
        gunBarrelVelocity.put("player_run", new Vector2(1, 0));
        gunBarrelVelocity.put("player_run_diagonal_down", new Vector2(0.707f, -0.707f));
        gunBarrelVelocity.put("player_jump_begin_up", new Vector2(0, 1));
        gunBarrelVelocity.put("player_jump_end_up", new Vector2(0, 1));
        gunBarrelVelocity.put("player_jump_begin_diagonal_up", new Vector2(0.707f, 0.707f));
        gunBarrelVelocity.put("player_jump_end_diagonal_up", new Vector2(0.707f, 0.707f));
        gunBarrelVelocity.put("player_jump_begin", new Vector2(1, 0));
        gunBarrelVelocity.put("player_jump_end", new Vector2(1, 0));
        gunBarrelVelocity.put("player_jump_begin_diagonal_down", new Vector2(0.707f, -0.707f));
        gunBarrelVelocity.put("player_jump_end_diagonal_down", new Vector2(0.707f, -0.707f));

        GameScene gameScene = engine.getGameScene();
        gameController = engine.getGameController();
        soundManager = engine.getSoundManager();
        entityManager = gameScene.getEntityManager();
        bulletFactory = gameScene.getBulletFactory();
        particlePool = gameScene.getParticlePool();
        grid = gameScene.getGrid();
        body = entity.getComponent(BodyComponent.class);
        velocity = entity.getComponent(VelocityComponent.class);
        velocity.setMaxX(config.getPlayerMaxRunningVelocity());
        miniBar = entity.getComponent(MiniBarComponent.class);
        health = entity.getComponent(HealthComponent.class);
        waterCollision = entity.getComponent(WaterCollisionComponent.class);

        miniBar.add(entity);

        // when bottom collision happens, call the touchFloor
        MessageListener floorTouchListener = (sender, message) -> touchFloor();
        messageHandler.subscribe(GridCollisionComponent.BOTTOM_COLLISION, floorTouchListener);
        messageHandler.subscribe(EntityCollisionComponent.BOTTOM_COLLISION, floorTouchListener);
        messageHandler.subscribe(MountableComponent.MOUNTED, floorTouchListener);

        // if the player still in a block after an entity collision, it must be killed
        MessageListener killCollisionListener = (sender, message) -> {
            Grid grid = engine.getGameScene().getGrid();
            if (grid.bodyInBlock(body)) {
                kill();
            }
        };
        messageHandler.subscribe(EntityCollisionComponent.LEFT_COLLISION, killCollisionListener);
        messageHandler.subscribe(EntityCollisionComponent.RIGHT_COLLISION, killCollisionListener);

        // set the sliding flag if we touch a sliding block
        messageHandler.subscribe(GridCollisionComponent.SLIDER_COLLISION, (sender, message) -> sliding = true);

        // health handling
        messageHandler.subscribe(HealthComponent.DECREASED, (sender, message) -> damage());
        messageHandler.subscribe(HealthComponent.ZERO, (sender, message) -> kill());

        // default acceleration
        acceleration = config.getPlayerAcceleration();
    }

    public void addAbility(PlayerAbility ability) {
        abilities.add(ability);
    }

    public boolean hasAbility(PlayerAbility ability) {
        return abilities.contains(ability);
    }

    public boolean hasAbility(PlayerAbility[] checkAbilities) {
        for (PlayerAbility checkAbility : checkAbilities) {
            if (abilities.contains(checkAbility)) {
                return true;
            }
        }
        return false;
    }

    public float getPower() {
        return 1.0f;
    }

    public void addPoint(int point) {
        if (point != 0) {
            score += point;
            messageHandler.send(SCORE_CHANGED);
        }
    }

    public int getScore() {
        return score;
    }


    public int getItemCount() {
        return itemCount;
    }

    public void incKnockoutCount() {
        knockoutCount++;
    }

    public int getKnockoutCount() {
        return knockoutCount;
    }

    public void incSecretCount() {
        secretCount++;
    }

    public int getSecretCount() {
        return secretCount;
    }


    public void jumpOnNextFrame() {
        jumpOnNextFrame = true;
    }

    public void setPushedBy(Entity value) {
        pushedBy = value;
    }

    public void incOxygen() {
        underWaterTime = 0;
    }

    private void switchWeapon() {
        if (hasAbility(PlayerAbility.CHANGE_WEAPON)) {
            currentWeaponIndex++;
            if (currentWeaponIndex > 2) {
                currentWeaponIndex = 0;
            }
        } else {
            currentWeaponIndex = currentWeaponIndex == 1 ? 0 : 1;
        }
        switch (currentWeaponIndex) {
            case 0:
            case 2:
                animPrefix = ""; break;
            case 1: animPrefix = "crowbar_"; break;
        }
    }

    public void setOnSwitch(Entity value) {
        onSwitch = value;
    }

    @Override
    public void preUpdate(float delta) { // if a value used by the physics, calculate it here
        if (entityManager.isInAnimation()) {
            return;
        }
        if (hasAbility(PlayerAbility.JUMPING) && gameController.wantsToDrop() && jumpReleased) {
            messageHandler.send(WANTS_TO_DROP);
            jumpReleased = false;
        }
    }

    @Override
    public void update(float delta) {
        if (entityManager.isInAnimation()) {
            return;
        }
        checkBlocks();
        handleJumpOnNextFrame();
        handleHorizontalVelocityForPain();
        handleVerticalVelocity(delta);
        handlePoisonedState(delta);
        if (!inPain) {
            handleHorizontalVelocity();
            handleGunPosition();
            handleFire();
        }
        adjustUnderWater(delta);
        adjustHealth(delta);
        adjustInPainTime(delta);
        adjustInvincibleBlink(delta);
        pickUpItems();

        changeDown = gameController.isYDown();
        if (hasAbility(PlayerAbility.CHANGE_WEAPON) && changeDown && !lastChangeDown) {
            switchWeapon();
        }

        if (attackTime > 0) {
            attackTime -= delta;
            if (attackTime < 0) {
                attackTime = 0;
            }
        }

        sliding = false;
    }

    private void pickUpItems() {

        // pick up items from the entities
        Set<Entity> items = entityManager.getAllByClassAndArea(ItemComponent.class, entity);
        if (items != null) {
            for (Entity itemEntity : items) {
                BodyComponent itemBody = itemEntity.getComponent(BodyComponent.class);
                if (body.isOverlap(itemBody)) {
                    ItemComponent item = itemEntity.getComponent(ItemComponent.class);
                    pickUp(item.getName(), itemBody.getLeft(), itemBody.getBottom());
                    itemEntity.remove();
                }
            }
        }

        // pick up items from the tilemap
        TiledMap map = engine.getGameScene().getTiledMap();
        TiledMapTileLayer layer1 = (TiledMapTileLayer)map.getLayers().get("Items1");
        TiledMapTileLayer layer2 = (TiledMapTileLayer)map.getLayers().get("Items2");
        if (layer1 == null || layer2 == null) {
            return;
        }
        int checkX1 = grid.getX(body.getLeft());
        int checkX2 = grid.getX(body.getRight());
        int checkY1 = grid.getY(body.getBottom());
        int checkY2 = grid.getY(body.getTop());
        for (int j = checkY1; j <= checkY2; j++) {
            for (int i = checkX1; i <= checkX2; i++) {
                TiledMapTileLayer.Cell cell1 = layer1.getCell(i, j);
                TiledMapTileLayer.Cell cell2 = layer2.getCell(i, j);
                String name = null;
                if (cell1 != null) {
                    MapProperties props = cell1.getTile().getProperties();
                    name = props.get("type").toString();
                    layer1.setCell(i, j, null);
                } else if (cell2 != null) {
                    MapProperties props = cell2.getTile().getProperties();
                    name = props.get("type").toString();
                    layer2.setCell(i, j, null);
                }
                if (name != null) {
                    pickUp(name, i * config.getTileWidth(), j * config.getTileHeight());
                }
            }
        }


    }

    private void pickUp(String name, float x, float y) {

        GameScreen gameScreen = (GameScreen)engine.getScreen("game");
        GameStage gameStage = (GameStage)gameScreen.getStage();

        // mechanics + hud
        switch (name) {
            case "lollipop":
                health.increase(0.1f);
                gameStage.showItemScore(x, y, "+10", 0.47f, 1f, 0f);
                addPoint(10);
                break;
            case "tequila":
                health.increase(0.05f);
                gameStage.showItemScore(x, y, "+5", 0.47f, 1f, 0f);
                addPoint(5);
                break;
            case "coin":
                gameStage.showItemScore(x, y, "+1", 1, 0.8f, 0.1f);
                addPoint(1);
                break;
        }

        itemCount++;

        // scene graphics
        soundManager.play("pickup");
        Entity particle = particlePool.obtain();
        BodyComponent body = particle.getComponent(BodyComponent.class);
        body.setSize(config.getTileWidth(), config.getTileHeight());
        body.setLeft(x);
        body.setBottom(y);
        VelocityComponent velocity = particle.getComponent(VelocityComponent.class);
        velocity.setY(140f);
        velocity.setGravity(config.getDefaultGravity());
        ViewComponent view = particle.getComponent(ViewComponent.class);
        view.setSprite(0, "pickup_" + name); // TODO: region name to item_ ?
        ParticleComponent particleComponent = particle.getComponent(ParticleComponent.class);
        particleComponent.setLifeTime(1);
        particleComponent.setAlphaChange(2);
        entityManager.add(particle);
    }

    @Override
    public void postUpdate(float delta) {
        adjustInAirTime(delta);
        adjustView(delta);
        wasInPain = inPain;
        lastAttackTime = attackTime;
        lastChangeDown = changeDown;
        lastPoisonedSecond = (int)poisonedTime;

    }

    private void handleJumpOnNextFrame() {
        if (!jumpOnNextFrame) {
            return;
        }
        jumpOnNextFrame = false;
        if (velocity.getY() <= 0) {
            velocity.setY(config.getPlayerJumpVelocityAddition());
        } else {
            velocity.setY(velocity.getY() + config.getPlayerJumpVelocityAddition() / 2);
        }
        addDust();
    }

    private void handleHorizontalVelocityForPain() {
        if (nextHorizontalVelocityForPain != 0) {
            velocity.setX(nextHorizontalVelocityForPain);
            nextHorizontalVelocityForPain = 0;
        }
    }

    private void adjustInvincibleBlink(float delta) {
        if (invincibleTime > 0) {
            invincibleBlinkTime += delta;
            if (invincibleBlinkTime > 0.1f) {
                invincibleBlinkTime = -0.1f;
            }
        }
    }

    private void adjustInPainTime(float delta) {
        if (inPain) {
            inPainTime -= delta;
            if (inPainTime < 0) {
                inPain = false;
            }
        }
    }

    private void adjustUnderWater(float delta) {
        lastHeadUnderWater = headUnderWater;
        lastFootUnderWater = footUnderWater;
        //boolean isInWater = waterCollision.isInWater();
        headUnderWater = grid.get(Grid.Layer.WATER, body.getCenterX(), body.getBottom() + 15f);
        footUnderWater = grid.get(Grid.Layer.WATER, body.getCenterX(), body.getBottom() + 6f);
        if (headUnderWater) {
            if (!lastHeadUnderWater) {
                underWaterTime = 0;
            }
            underWaterTime += delta;
            miniBar.setVisibleTime(0.5f);
        }
        if (underWaterTime > maxUnderWaterTime) {
            oxygenDamage();
        }
        miniBar.adjustDisplay(1f - (underWaterTime / maxUnderWaterTime), delta);
    }

    private void checkBlocks() {
        if (grid.bodyIn(body, Grid.Layer.SPIKE)) {
            health.decrease(0.5f, null);
        } else if (grid.bodyIn(body, Grid.Layer.POISON)) {
            health.decrease(0.3f, null);
            poisonedTime = 10f; // TODO: constant
        } else if (grid.bodyIn(body, Grid.Layer.DEATH)) {
            kill();
        }
    }

    private void handleGunPosition() {
        if (gameController.getAxisY() > config.getPlayerMinVerticalAxis()
            && Math.abs(gameController.getAxisX()) <= config.getPlayerMinHorizontalAxis()) {
            gunPosition = GunPosition.UP;
        } else if (gameController.getAxisY() > config.getPlayerMinVerticalAxis() / 2
            && Math.abs(gameController.getAxisX()) > config.getPlayerMinHorizontalAxis()) {
            gunPosition = GunPosition.DIAGONAL_UP;
        } else if (gameController.getAxisY() < -config.getPlayerMinVerticalAxis() / 2) {
            gunPosition = GunPosition.DIAGONAL_DOWN;
        } else {
            gunPosition = GunPosition.FORWARD;
        }
    }

    private void handleFire() {
        if (!hasAbility(PlayerAbility.FIRING)) {
            return;
        }
        boolean xDown = gameController.isXDown();
        if (onSwitch == null) {
            if (xDown && !lastXDown && attackTime <= 0) {
                fire();
            }
        } else {
            if (xDown && !lastXDown) {
                KnifeSwitchComponent ksw = onSwitch.getComponent(KnifeSwitchComponent.class);
                ksw.use();
            }
        }
        lastXDown = xDown;
    }

    private void adjustHealth(float delta) {
        if (invincibleTime > 0) {
            invincibleTime -= delta;
            if (invincibleTime < 0) {
                health.setActive(true);
            }
        }
        if (body.getTop() < -10) {
            health.setActive(true);
            health.decrease(1000.0f, null);
        }
    }

    private boolean isHorizontalDown() {
        return Math.abs(gameController.getAxisX()) > config.getPlayerMinHorizontalAxis();
    }

    private void handleHorizontalVelocity() {
        if (!hasAbility(PlayerAbility.MOVE)) {
            return;
        }

        float divider = sliding ? 5 : (waterCollision.isInWater() ? 3 : 1); // TODO: config
        float vMaxX = config.getPlayerMaxRunningVelocity();
        float baseAcceleration = acceleration / divider;
        if (footUnderWater) {
            if (body.isInAir()) {
                velocity.setMaxX(vMaxX / 1.3f);
            } else {
                velocity.setMaxX(vMaxX / 2f);
            }
        } else {
            velocity.setMaxX(vMaxX);
        }
        boolean horizontalDown = isHorizontalDown();
        if (horizontalDown) {
            float xAxis = gameController.getAxisX();
            // TODO: do it better
            // sticky problem solution
            float multiplier = 1f;
            Entity parent = entity.getParent();
            if (parent != null) {
                VelocityComponent parentVelocity = parent.getComponent(VelocityComponent.class);
                if (body.isSideCollided() && Math.signum(parentVelocity.getX()) != Math.signum(xAxis)) {
                    multiplier = 100f;
                }
            }
            if (pushedBy != null) {
                VelocityComponent pushedByVelocity = pushedBy.getComponent(VelocityComponent.class);
                if (body.isSideCollided() && Math.signum(pushedByVelocity.getGlobalX()) == Math.signum(xAxis)) {
                    multiplier = 100f;
                }
                pushedBy = null;
            }
            //
            float a = Math.signum(xAxis) * baseAcceleration * multiplier;
            if (body.isSideCollided() || !lastHorizontalDown || Math.signum(a) != Math.signum(velocity.getAcceleration())) {
                velocity.setAcceleration(a);
                velocity.setInitialX();
            }
            horizontalSign = Math.signum(velocity.getX());
        } else {
            if (lastHorizontalDown) {
                velocity.setAcceleration(-horizontalSign * baseAcceleration);
                velocity.setInitialX();
            }
            if (velocity.getX() != 0 && Math.signum(velocity.getX()) != horizontalSign) {
                velocity.setAcceleration(0);
                velocity.setX(0);
            }
        }
        lastHorizontalDown = horizontalDown;
    }

    private void handleVerticalVelocity(float delta) {
        if (!hasAbility(PlayerAbility.JUMPING)) {
            return;
        }

        // set velocity by water
        if (footUnderWater && !lastFootUnderWater) {
            velocity.setGravity(config.getDefaultGravity() / 5f);
            velocity.setMaxY(config.getMaxVelocity() / 10f); // divide max velocity
            if (velocity.getY() < -60) {
                velocity.setY(-30);
            } else {
                velocity.setY(velocity.getY() / 3f);
            }
        } else if (!footUnderWater && lastFootUnderWater) {
            velocity.setMaxY(config.getMaxVelocity()); // set back max velocity
            // to be able to get out from the water
            velocity.setGravity(config.getDefaultGravity());
            if (velocity.getY() < 190) {
                velocity.setY(190);
            }
        }

        if (!inPain) {
            if (!gameController.isADown()) {
                jumpReleased = true;
            }
            int maxJumpCount = hasAbility(PlayerAbility.DOUBLE_JUMP) ? 2 : 1;
            if (gameController.isADown() && jumpReleased && (jumpCounter < maxJumpCount || headUnderWater)) {
                float vy = headUnderWater ? config.getPlayerJumpVelocity() / 2.8f : config.getPlayerJumpVelocity();
                velocity.setY(vy);
                jumpCounter++;
                jumpReleased = false;
                if (headUnderWater) {
                    soundManager.playRandom(swimSounds);
                }
                //soundManager.play("jump");
                addDust();
            }
        }

        // bugfix for "sticky on the bottom when moving down"
        if (pushedBy != null && body.isTopCollided()) {
            VelocityComponent pusherVelocity = pushedBy.getComponent(VelocityComponent.class);
            float pvy = pusherVelocity.getGlobalY();
            if (pvy < 0) {
                velocity.setY(pvy < -200 ? -200 : pvy * 1.1f);
            }
        }

        // if we moving and we just pressed a horizontal button or we are in animation set the flipX
        if (velocity.getX() != 0 && !inPain) {
            if (entityManager.isInAnimation()) {
                flipX = velocity.getX() < 0;
            } else if (isHorizontalDown()) {

                // wonderful solution for the joystick "bounce" issue
                float axisX = gameController.getAxisX();
                int axisXSignCurrent = (int)Math.signum(axisX);
                if (axisXSign == 0) {
                    axisXSign = axisXSignCurrent;
                    flipX = axisX < 0;
                } else if (axisXSignCurrent == axisXSign) {
                    axisXSignTime += delta;
                    if (axisXSignTime > 0.025) {
                        flipX = axisX < 0;
                    }
                } else {
                    axisXSign = axisXSignCurrent;
                    axisXSignTime = 0;
                }
            }
        }
    }

    private void adjustInAirTime(float delta) {
        if (body.isInAir()) {
            inAirTime += delta;
        } else {
            inAirTime = 0;
        }
        if (inAirTime > 0.15f && jumpCounter == 0) {
            jumpCounter = 1;
        }
    }

    private void setAnimation(String animationName) {
        ViewComponent view = entity.getComponent(ViewComponent.class);
        String animPostfix = gunPosition.getName() != null ? "_" + gunPosition.getName() : "";
        currentAnimationName = "player_" + animPrefix + animationName + animPostfix;
        view.setAnimation(0, currentAnimationName);
    }

    private void setAnimationTime(float time) {
        ViewComponent view = entity.getComponent(ViewComponent.class);
        view.setAnimationTime(0, time);
    }

    private void adjustView(float delta) {

        ViewComponent view = entity.getComponent(ViewComponent.class);

        if (inPain && !entityManager.isInAnimation()) {

            // if player is in pain, that's the priority animation
            if (!wasInPain) {
                setAnimationTime(0);
                setAnimation("pain");
            }

        } else {

            if (attackTime > 0) {
                // if the attack just started
                if (lastAttackTime == 0) {
                    setAnimationTime(0);
                    setAnimation("attack1");
                }

            } else if (!body.isInAir()) {

                if (body.wasInAir()) { // if just fallen down
                    if (!view.isPaused()) {
                        addDust();
                        soundManager.playRandom(fallDownSounds);
                    }
                } else {
                    // set the "idle" or "run" animation regarding the velocity, controller and animation state
                    setAnimation((velocity.getX() == 0 || (!lastHorizontalDown && !entityManager.isInAnimation())) ? "idle" : "run");
                    // if not in air and started to move horizontally, reset the animation time
                    if (velocity.getX() != 0 && velocity.getLastX() == 0) {
                        setAnimationTime(0);
                    }
                }

            } else {
                if (footUnderWater) {
                    setAnimation("swim");
                } else if (!body.wasInAir()) {
                    // if started to jump
                    setAnimation("jump_begin");
                    setAnimationTime(0);
                } else if (velocity.getY() > 0) {
                    setAnimation("jump_begin");
                } else if (velocity.getY() < 0 && velocity.getLastY() >= 0) {
                    // if started to fall down
                    setAnimation("jump_end");
                }
            }
        }

        view.flipX(flipX);

        // set the to no transparent and blend to white
        for (int i = 0; i < miniBar.getViewIndex(); i++) {
            view.setAlpha(i, 1);
            view.setColor(i, 1, 1, 1);
        }
        // if invincible, set alpha to 0.3 if needed
        if (invincibleTime > 0) {
            float a = invincibleBlinkTime < 0 ? 1f : 0.4f;
            for (int i = 0; i < miniBar.getViewIndex(); i++) {
                view.setAlpha(i, a);
            }
        }
        // if poisoned, set blend to green if needed
        if (poisonedTime > 0) {
            for (int i = 0; i < miniBar.getViewIndex(); i++) {
                float c = .5f + .5f * (float)Math.sin(poisonedTime * 15.7f);
                view.setColor(i, c, 1, c);
            }
        }
    }

    private void handlePoisonedState(float delta) {
        if (poisonedTime > 0) {
            poisonedTime -= delta;
            if ((int)poisonedTime != lastPoisonedSecond) {
                poisonDamage = true;
                health.decrease(0.05f, null);
            }
            if (poisonedTime < 0) {
                poisonedTime = 0;
            }
        }
    }

    private void oxygenDamage() {
        invincibleTime = 1f;
        underWaterTime = maxUnderWaterTime - invincibleTime;
        oxygenDamage = true;
        health.decrease(0.2f, null);
    }

    private void damage() {
        soundManager.play("ouch");
        if (poisonDamage) {
            poisonDamage = false;
            return;
        }
        if (!oxygenDamage) {
            invincibleTime = config.getPlayerInactiveHealthTime();
            adjustNextHorizontalVelocityForPain();
            jumpOnNextFrame();
        }
        health.setActive(false);
        oxygenDamage = false;
        inPainTime = config.getPlayerPainTime();
        inPain = true;

    }

    private void adjustNextHorizontalVelocityForPain() {
        velocity.setX(0);
        velocity.setAcceleration(0);
        Entity byEntity = health.getDecreasedByEntity();
        float multiplier = 0;
        if (byEntity != null) {
            BodyComponent otherBody = byEntity.getComponent(BodyComponent.class);
            Direction d = body.getDirection(otherBody);
            if (d == Direction.LEFT) {
                multiplier = 1f;
            } else if (d == Direction.RIGHT) {
                multiplier = -1f;
            }
        }
        nextHorizontalVelocityForPain = config.getPlayerPainHorizontalVelocity() * multiplier;
    }

    private void kill() {

        entity.setParent(null);

        setActive(false);

        ViewComponent view = entity.getComponent(ViewComponent.class);
        view.setVisible(false);

        velocity.setActive(false);
        health.setActive(false);

        soundManager.play("die");
        entity.setParent(null);
        GameScreen gameScreen = (GameScreen)engine.getScreen("game");
        gameScreen.prepareForGameOver();
    }

    public void revive(Vector2 startPosition) {
        nextHorizontalVelocityForPain = 0;
        invincibleTime = config.getPlayerInactiveHealthTime();

        setActive(true);

        BodyComponent playerBody = entity.getComponent(BodyComponent.class);
        playerBody.setGlobalX(startPosition.x);
        playerBody.setGlobalY(startPosition.y + GridCollisionComponent.CORRIGATION);

        poisonedTime = 0;

        health.revive();
        health.setActive(true);

        velocity.setX(0);
        velocity.setY(0);
        velocity.setGravity(config.getDefaultGravity());
        velocity.setActive(true);

        ViewComponent view = entity.getComponent(ViewComponent.class);
        view.setVisible(true);

        underWaterTime = 0;
    }

    private void touchFloor() {
        jumpCounter = 0;
    }

    public Vector2 getCurrentGunBarrelPosition() {
        Vector2 gunBPos = gunBarrelPosition.get(currentAnimationName);
        if (gunBPos == null) {
            return null;
        }
        return new Vector2(body.getCenterX() + (flipX ? -1 : 1) * gunBPos.x, body.getBottom() + gunBPos.y);
    }

    private void fire() {

        Vector2 a = new Vector2(body.getCenterX(), body.getCenterY());
        Vector2 b = getCurrentGunBarrelPosition();
        if (b == null) {
            return;
        }
        if (grid.getIntersection(a, b, null) || entityManager.getIntersection(a, b, null, entity)) {
            return;
        }

        Vector2 gunBVel = gunBarrelVelocity.get(currentAnimationName);

        BulletOptions bo = new BulletOptions();
        bo.ownerBody = body;
        bo.positionX = b.x;
        bo.positionY = b.y;
        bo.velocityX = (flipX ? -1 : 1) * gunBVel.x * 1000f;
        bo.velocityY = gunBVel.y * 1000f;
        //bo.sprite = "player_bullet1";

        bulletFactory.create(bo);
    }

    private void addDust() {
        if (headUnderWater) {
            return;
        }
        Entity dust = particlePool.obtain();
        ParticleComponent particle = dust.getComponent(ParticleComponent.class);
        particle.setLifeTime(0.3f);
        BodyComponent dustBody = dust.getComponent(BodyComponent.class);
        dustBody.setSize(16, 16);
        dustBody.setAlign(Align.CENTER_BOTTOM);
        dustBody.setX(body.getGlobalX());
        dustBody.setY(body.getGlobalY());
        ViewComponent view = dust.getComponent(ViewComponent.class);
        view.setAnimation(0, "dust");
        view.setAlign(0, Align.CENTER_BOTTOM);
        entityManager.add(dust);
    }

}
