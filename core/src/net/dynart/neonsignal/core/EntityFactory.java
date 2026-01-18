package net.dynart.neonsignal.core;

import com.badlogic.gdx.Gdx;

import net.dynart.neonsignal.components.ActionComponent;
import net.dynart.neonsignal.components.BlockComponent;
import net.dynart.neonsignal.components.BulletSpawnerComponent;
import net.dynart.neonsignal.components.ButtonComponent;
import net.dynart.neonsignal.components.CameraLimitTriggerComponent;
import net.dynart.neonsignal.components.EnemyBlockComponent;
import net.dynart.neonsignal.components.KillSwitchComponent;
import net.dynart.neonsignal.components.KnifeSwitchComponent;
import net.dynart.neonsignal.components.OverlapAttackableComponent;
import net.dynart.neonsignal.components.GridCollisionComponent;
import net.dynart.neonsignal.components.BodyComponent;
import net.dynart.neonsignal.components.BoxComponent;
import net.dynart.neonsignal.components.ColliderComponent;
import net.dynart.neonsignal.components.CrushComponent;
import net.dynart.neonsignal.components.DisappearingBlockComponent;
import net.dynart.neonsignal.components.EntityCollisionComponent;
import net.dynart.neonsignal.components.ExitComponent;
import net.dynart.neonsignal.components.FallingComponent;
import net.dynart.neonsignal.components.ActivateOnScreenComponent;
import net.dynart.neonsignal.components.ItemComponent;
import net.dynart.neonsignal.components.MovableComponent;
import net.dynart.neonsignal.components.OxygenComponent;
import net.dynart.neonsignal.components.PlatformComponent;
import net.dynart.neonsignal.components.PusherComponent;
import net.dynart.neonsignal.components.ReviveComponent;
import net.dynart.neonsignal.components.SceneWarpComponent;
import net.dynart.neonsignal.components.SecretComponent;
import net.dynart.neonsignal.components.ConveyorComponent;
import net.dynart.neonsignal.components.SpikeComponent;
import net.dynart.neonsignal.components.SpringboardComponent;
import net.dynart.neonsignal.components.StartFallingOnMountComponent;
import net.dynart.neonsignal.components.SwitchComponent;
import net.dynart.neonsignal.components.SwitchableComponent;
import net.dynart.neonsignal.components.VelocityComponent;
import net.dynart.neonsignal.components.ViewComponent;
import net.dynart.neonsignal.core.utils.Align;
import net.dynart.neonsignal.core.utils.Parameters;
import net.dynart.neonsignal.core.utils.StringUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@SuppressWarnings("unused") // the methods called by reflection
public class EntityFactory {

    public static final String LOG_TAG = "EntityFactory";

    protected EngineConfig config;
    protected Engine engine;
    protected EntityManager entityManager;

    public void postConstruct(Engine engine) {
        this.engine = engine;
        config = engine.getConfig();
        GameScene gameScene = engine.getGameScene();
        entityManager = gameScene.getEntityManager();
    }

    public Entity create(Parameters parameters) {
        String type = parameters.get("type", null);
        if (type == null) {
            return null;
        }
        Entity entity;
        String methodName = "create" + StringUtil.camelize(type);
        try {
            Gdx.app.log(LOG_TAG, "Calling " + methodName);
            for (String k : parameters.getKeySet()) {
                Gdx.app.log(LOG_TAG, "  " + k + ": " + parameters.get(k));
            }
            Method method = getClass().getMethod(methodName, Parameters.class);
            entity = (Entity)method.invoke(this, parameters);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Can't create entity (no such method): " + type);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Can't create entity (illegal access): " + type);
        } catch (InvocationTargetException e) {
            // Use breakpoint/debug when this happens!
            e.printStackTrace();
            throw new RuntimeException("Can't create entity (create error): " + type);
        }
        String group = parameters.get("group", null);
        if (group != null) {
            entityManager.addToGroup(group, entity);
            entity.setGroup(group);
        }
        entity.setActive(parameters.getBoolean("active", false));
        entity.setName(parameters.get("name", null));
        entity.postConstruct();
        return entity;
    }

    protected BodyComponent createBody(Parameters parameters) {
        return createBody(parameters, Align.CENTER_BOTTOM);
    }

    protected BodyComponent createBody(Parameters parameters, Align align) {
        float width = parameters.getFloat("width");
        float height = parameters.getFloat("height");
        float x = parameters.getFloat("x");
        float y = parameters.getFloat("y");
        return createBody(parameters, width, height, align);
    }

    protected BodyComponent createBody(Parameters parameters, float width, float height) {
        return createBody(parameters, width, height, Align.CENTER_BOTTOM);
    }

    protected BodyComponent createBody(Parameters parameters, float width, float height, Align align) {
        float editorWidth = parameters.getFloat("width");
        float editorHeight = parameters.getFloat("height");
        float editorX = parameters.getFloat("x");
        float editorY = parameters.getFloat("y");
        BodyComponent body = new BodyComponent(width, height);
        body.setX(editorX + editorWidth / 2f * -align.getLeftMultiplier());
        body.setY(editorY + editorHeight / 2f * -align.getBottomMultiplier());
        body.setAlign(align);
        return body;
    }

    protected BodyComponent createBody(float width, float height, float x, float y) {
        BodyComponent body = new BodyComponent(width, height);
        body.setX(x);
        body.setY(y);
        return body;
    }

    protected BodyComponent createBody(float width, float height, float x, float y, Align align) {
        BodyComponent body = new BodyComponent(width, height);
        body.setX(x);
        body.setY(y);
        body.setAlign(align);
        return body;
    }

    protected VelocityComponent createGravity(float gravity) {
        VelocityComponent velocity = new VelocityComponent();
        velocity.setGravity(gravity);
        return velocity;
    }

    protected ViewComponent createSprite(String regionName) {
        return createSprite(regionName, Align.CENTER_BOTTOM);
    }

    protected ViewComponent createSprite(String regionName, Align align) {
        ViewComponent viewComponent = new ViewComponent(engine);
        int index = viewComponent.getSpriteCount();
        viewComponent.addSprite(new GameSprite());
        viewComponent.setSprite(index, regionName);
        viewComponent.setAlign(index, align);
        return viewComponent;
    }

    protected ViewComponent createAnimation(String animationName) {
        return createAnimation(animationName, Align.CENTER_BOTTOM, false);
    }

    protected ViewComponent createAnimation(String animationName, boolean flipX) {
        return createAnimation(animationName, Align.CENTER_BOTTOM, flipX);
    }

    protected ViewComponent createAnimation(String animationName, Align align) {
        return createAnimation(animationName, align, false);
    }

    protected ViewComponent createAnimation(String animationName, Align align, boolean flipX) {
        ViewComponent viewComponent = new ViewComponent(engine);
        int index = viewComponent.getSpriteCount();
        viewComponent.addSprite(new GameSprite());
        viewComponent.setAnimation(index, animationName);
        viewComponent.flipX(flipX);
        viewComponent.setAlign(index, align);
        return viewComponent;
    }

    protected Entity createItem(Parameters parameters) {
        String type = parameters.get("type");
        Entity item = new Entity(engine);
        item.addComponents(
            createBody(parameters, 16, 16),
            new ItemComponent(type),
            new ActivateOnScreenComponent(),
            createSprite("pickup_" + type) // TODO: region name to item_ ?
        );
        return item;
    }

    public Entity createBox(Parameters parameters) {
        parameters.set("active", true);
        Entity result = new Entity(engine);
        result.addComponents(
            createBody(parameters, 15.99f, 15.99f),
            createGravity(config.getDefaultGravity()),
            /*
            new PlatformComponent(),
            new MountableComponent(),
            new PusherComponent(),
            */
            new GridCollisionComponent(),
            new EntityCollisionComponent(),
            new BlockComponent(),
            new OverlapAttackableComponent(),
            new ColliderComponent(),
            new CrushComponent("box", "box_crush"),
            new BoxComponent(parameters.get("item", null)),
            //new ActivateOnScreenComponent(),
            createSprite("box")
        );

        return result;
    }

    public Entity createPlatform(Parameters parameters) {
        String pathName = parameters.get("path");
        Entity result = new Entity(engine);
        // if it doesn't have a path, it should be active by default
        if (pathName == null) {
            parameters.set("active", true);
        }
        result.addComponents(
            createBody(parameters, Align.LEFT_BOTTOM),
            new VelocityComponent(),
            new PlatformComponent()
        );
        if (pathName != null) {
            result.addComponents(
                new SwitchableComponent(parameters.get("inputs")),
                new MovableComponent(
                    parameters.get("path"),
                    parameters.getFloat("speed", 60.0f),
                    parameters.getBoolean("slowing", true),
                    parameters.getInteger("start_index", 0),
                    parameters.get("move_type", MovableComponent.FORWARD_CONTINUOUS),
                    parameters.getFloat("wait_time", 0),
                    false,
                    parameters.getBoolean("start", false)
                )
            );
            if (parameters.getBoolean("start")) {
                // if it has to start, it should start when on the screen
                result.addComponent(new ActivateOnScreenComponent());
            } else {
                // if it is switchable, should be active full time
                parameters.set("active", true);
            }
        }
        String spriteName = parameters.get("sprite");
        if (spriteName != null) {
            ViewComponent viewComponent = createSprite(spriteName, Align.LEFT_BOTTOM);
            result.addComponent(viewComponent);
            GameSprite sprite = viewComponent.getSprite(0);
            sprite.setOffsetY(sprite.getOffsetY() + parameters.getInteger("offset", 0));
        }
        return result;
    }

    public Entity createDplatform(Parameters parameters) {
        Entity result = new Entity(engine);
        boolean visible = parameters.getBoolean("visible", true);
        String animPrefix = parameters.get("sprite", "dplatform");
        ViewComponent viewComponent = createAnimation(visible ? animPrefix + "_in" : animPrefix + "_out");
        GameSprite sprite = viewComponent.getSprite(0);
        sprite.setOffsetY(sprite.getOffsetY() + parameters.getInteger("offset", 0));
        result.addComponents(
            createBody(parameters),
            new OverlapAttackableComponent(),
            new ColliderComponent(),
            new DisappearingBlockComponent(
                parameters.getFloat("time", 1f),
                visible, animPrefix
            ),
            new ActivateOnScreenComponent(),
            viewComponent
        );
        return result;
    }

    public Entity createConveyor(Parameters parameters) {

        parameters.set("active", true);

        // create the entity that will be used as the parent for the velocity
        Entity moverEntity = new Entity(engine);
        moverEntity.addComponents(
            createBody(parameters, 8, 8),
            new VelocityComponent()
        );
        entityManager.addToLists(moverEntity);
        moverEntity.postConstruct();

        // create the entity that will be used as the platform we can mount on
        Entity result = new Entity(engine);
        result.addComponents(
            createBody(parameters),
            new ColliderComponent(),
            new BlockComponent(),
            new OverlapAttackableComponent(),
            new PlatformComponent(),
            new ConveyorComponent(
                parameters.getFloat("speed", 0),
                moverEntity
            )
        );
        return result;
    }

    public Entity createAction(Parameters parameters) {
        parameters.set("active", true);
        Entity result = new Entity(engine);
        result.addComponents(
            createBody(parameters),
            new ActionComponent(
                parameters.get("path", ""),
                parameters.getBoolean("cutscene", false),
                parameters.getBoolean("once", true)
            )
        );
        return result;
    }

    public Entity createTarget(Parameters parameters) {
        Entity result = new Entity(engine);
        result.addComponents(
            createBody(parameters)
        );
        return result;
    }

    public Entity createSecret(Parameters parameters) {
        parameters.set("active", true);
        Entity result = new Entity(engine);
        result.addComponents(
            createBody(parameters),
            new SecretComponent()
        );
        return result;
    }

    public Entity createSpike(Parameters parameters) {
        parameters.set("active", true);
        Entity result = new Entity(engine);
        ViewComponent view = createSprite("spike", Align.LEFT_BOTTOM);
        view.setRepeatX((int)(parameters.getFloat("width") / config.getTileWidth()));
        result.addComponents(
            createBody(parameters, Align.LEFT_BOTTOM),
            view,
            new ColliderComponent(),
            new BlockComponent(),
            new PlatformComponent(),
            new SpikeComponent()
        );
        return result;
    }

    public Entity createFallingPlatform(Parameters parameters) {
        Entity result = new Entity(engine);
        result.addComponents(
            createBody(parameters),
            new VelocityComponent(),
            new PlatformComponent(),
            new FallingComponent(-660f, 0.5f, "falling_floor", true),
            new StartFallingOnMountComponent(),
            new ActivateOnScreenComponent(),
            createSprite(
                parameters.get("sprite", "falling_floor1")
            )
        );
        return result;
    }

    public Entity createExit(Parameters parameters) {
        parameters.set("active", true);
        Entity result = new Entity(engine);
        result.addComponents(
            createBody(parameters),
            new ExitComponent(
                parameters.getBoolean("left")
            )
        );
        return result;
    }

    public Entity createBulletSpawner(Parameters parameters) {
        parameters.set("active", true);
        Entity result = new Entity(engine);
        result.addComponents(
            createBody(parameters),
            new BulletSpawnerComponent()
        );
        return result;
    }

    public Entity createSpringboard(Parameters parameters) {
        Entity spring = new Entity(engine);
        spring.addComponents(
            createBody(parameters),
            createSprite("springboard_bottom")
        );
        entityManager.addToLists(spring);
        spring.postConstruct();

        Entity result = new Entity(engine);
        result.addComponents(
            createBody(20, 5, parameters.getFloat("x") + 10f, parameters.getFloat("y") + 8f),
            new VelocityComponent(),
            new PlatformComponent(),
            new SpringboardComponent(spring, parameters.getFloat("speed", config.getPlayerJumpVelocity() * 2f)),
            new ActivateOnScreenComponent(),
            createSprite("springboard_top")
        );
        return result;
    }

    public Entity createMovable(Parameters parameters) {
        Entity movable = new Entity(engine);
        movable.addComponents(
            createBody(parameters, Align.LEFT_BOTTOM),
            createSprite(parameters.get("sprite", "movable1"), Align.LEFT_BOTTOM),
            new OverlapAttackableComponent(),
            new VelocityComponent(),
            new BlockComponent(),
            new PlatformComponent(),
            new ColliderComponent(),
            new PusherComponent(),
            new MovableComponent(
                parameters.get("path"),
                parameters.getFloat("speed", 60.0f),
                parameters.getBoolean("slowing", true),
                parameters.getInteger("start_index", 0),
                parameters.get("move_type", MovableComponent.FORWARD_CONTINUOUS),
                parameters.getFloat("wait_time", 0),
                false,
                parameters.getBoolean("start")
            ),
            new SwitchableComponent(parameters.get("inputs"))
        );
        if (parameters.getBoolean("start")) {
            // if it has to start, it should start when on the screen
            movable.addComponent(new ActivateOnScreenComponent());
        } else {
            // if it is switchable, should be active full time
            parameters.set("active", true);
        }
        return movable;
    }

    public Entity createButton(Parameters parameters) {
        Entity button = new Entity(engine);
        button.addComponents(
            createBody(parameters),
            createSprite(parameters.get("sprite", "button1_off")),
            new VelocityComponent(),
            new ColliderComponent(),
            new BlockComponent(),
            new PlatformComponent(),
            new SwitchComponent(
                parameters.get("names", ""),
                parameters.getBoolean("inverse", false),
                parameters.getFloat("repeat_time", 0)
            ),
            new ButtonComponent("button1_on", "button1_off"),
            new ActivateOnScreenComponent()
        );
        return button;
    }

    public Entity createSwitch(Parameters parameters) {
        parameters.set("active", true);
        Entity result = new Entity(engine);
        result.addComponents(
            createBody(parameters),
            createSprite(parameters.get("sprite", "switch_blue_off")),
            new SwitchComponent(
                parameters.get("names", ""),
                parameters.getBoolean("inverse", false),
                parameters.getFloat("repeat_time", 0)
            ),
            new KnifeSwitchComponent("switch_blue_on", "switch_blue_off")
        );
        return result;
    }

    public Entity createCameraLimitTrigger(Parameters parameters) {
        parameters.set("active", true);
        Entity camLimit = new Entity(engine);
        camLimit.addComponents(
            createBody(parameters),
            new CameraLimitTriggerComponent(
                parameters.get("left"),
                parameters.get("right"),
                parameters.get("top"),
                parameters.get("bottom"),
                parameters.getBoolean("fade"),
                parameters.getBoolean("instant"),
                parameters.getBoolean("in_air_check", true)
            )
        );
        return camLimit;
    }

    public Entity createSceneWarp(Parameters parameters) {
        parameters.set("active", true);
        Entity warp = new Entity(engine);
        warp.addComponents(
            createBody(parameters),
            new SceneWarpComponent(
                parameters.get("target"),
                parameters.getBoolean("fade")
            ),
            new CameraLimitTriggerComponent(
                parameters.get("left"),
                parameters.get("right"),
                parameters.get("top"),
                parameters.get("bottom"),
                parameters.getBoolean("fade"),
                parameters.getBoolean("instant"),
                false
            )
        );
        return warp;
    }

    public Entity createRevive(Parameters parameters) {
        parameters.set("active", true);
        Entity revive = new Entity(engine);
        revive.addComponents(
            createBody(parameters),
            new ReviveComponent()
        );
        return revive;
    }

    public Entity createKillSwitch(Parameters parameters) {
        parameters.set("active", true);
        Entity killSwitch = new Entity(engine);
        killSwitch.addComponents(
            createBody(parameters),
            new SwitchComponent(
                parameters.get("names", ""),
                parameters.getBoolean("inverse", false),
                parameters.getFloat("repeat_time", 0)
            ),
            new KillSwitchComponent()
        );
        return killSwitch;
    }

    public Entity createOxygen(Parameters parameters) {
        Entity oxygen = new Entity(engine);
        oxygen.addComponents(
            createBody(parameters, 20, 20),
            createAnimation("oxygen_idle"),
            new OxygenComponent(),
            new ActivateOnScreenComponent()
        );
        return oxygen;
    }

    public Entity createEnemyBlock(Parameters parameters) {
        parameters.set("active", true);
        Entity enemyBlock = new Entity(engine);
        enemyBlock.addComponents(
            createBody(parameters),
            new ColliderComponent(),
            new EnemyBlockComponent()
        );
        return enemyBlock;
    }

    public Entity createDecoration(Parameters parameters) {
        Entity decoration = new Entity(engine);
        ViewComponent viewComponent = parameters.get("sprite").equals("")
            ? createAnimation(parameters.get("animation"))
            : createSprite(parameters.get("sprite"));
        viewComponent.flipX(parameters.getBoolean("flip_x", false));
        viewComponent.flipY(parameters.getBoolean("flip_y", false));
        viewComponent.setLayer(parameters.getInteger("layer", 100));
        if (parameters.has("visible")) {
            viewComponent.setVisible(parameters.getBoolean("visible"));
        }
        decoration.addComponents(
            createBody(parameters),
            new VelocityComponent(),
            new MovableComponent(
                parameters.get("path"),
                parameters.getFloat("speed", 60.0f),
                parameters.getBoolean("slowing", true),
                parameters.getInteger("start_index", 0),
                parameters.get("move_type", MovableComponent.FORWARD_CONTINUOUS),
                parameters.getFloat("wait_time", 0),
                false,
                parameters.getBoolean("start", false)
            ),
            viewComponent
        );
        return decoration;
    }

}
