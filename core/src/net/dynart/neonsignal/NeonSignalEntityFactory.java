package net.dynart.neonsignal;

import net.dynart.neonsignal.components.BlockComponent;
import net.dynart.neonsignal.components.BodyComponent;
import net.dynart.neonsignal.components.FlyType;
import net.dynart.neonsignal.components.ElectricSpikeComponent;
import net.dynart.neonsignal.components.EnemyBlockComponent;
import net.dynart.neonsignal.components.FrogType;
import net.dynart.neonsignal.components.RailEndComponent;
import net.dynart.neonsignal.components.RushComponent;
import net.dynart.neonsignal.components.MiniBarComponent;
import net.dynart.neonsignal.components.MountableComponent;
import net.dynart.neonsignal.components.FrogComponent;
import net.dynart.neonsignal.components.OverlapAttackableComponent;
import net.dynart.neonsignal.components.GridCollisionComponent;
import net.dynart.neonsignal.components.ColliderComponent;
import net.dynart.neonsignal.components.EnemyComponent;
import net.dynart.neonsignal.components.EntityCollisionComponent;
import net.dynart.neonsignal.components.FallingComponent;
import net.dynart.neonsignal.components.ActivateOnScreenComponent;
import net.dynart.neonsignal.components.PlatformComponent;
import net.dynart.neonsignal.components.PusherComponent;
import net.dynart.neonsignal.components.SplashComponent;
import net.dynart.neonsignal.components.StartFallingInDistanceComponent;
import net.dynart.neonsignal.components.SwitchableComponent;
import net.dynart.neonsignal.components.TramComponent;
import net.dynart.neonsignal.components.UfoComponent;
import net.dynart.neonsignal.components.ViewComponent;
import net.dynart.neonsignal.components.WalkerComponent;
import net.dynart.neonsignal.components.HealthComponent;
import net.dynart.neonsignal.components.JumperComponent;
import net.dynart.neonsignal.components.OverlapAttackComponent;
import net.dynart.neonsignal.components.PlayerComponent;

import net.dynart.neonsignal.core.Entity;

import net.dynart.neonsignal.core.GameSprite;
import net.dynart.neonsignal.core.utils.Align;
import net.dynart.neonsignal.core.utils.Parameters;
import net.dynart.neonsignal.components.CrushComponent;
import net.dynart.neonsignal.components.MovableComponent;
import net.dynart.neonsignal.components.VelocityComponent;
import net.dynart.neonsignal.components.WaterCollisionComponent;
import net.dynart.neonsignal.core.EntityFactory;

@SuppressWarnings("unused") // the methods called by reflection
public class NeonSignalEntityFactory extends EntityFactory {

    public Entity createPlayer(Parameters parameters) {
        parameters.set("active", true);
        Entity result = new Entity(engine);
        String direction = parameters.get("direction", "right");
        EntityCollisionComponent entityCollision = new EntityCollisionComponent();
        entityCollision.setExcludeComponentClass(EnemyBlockComponent.class);
        result.addComponents(
            createBody(parameters, 94, 190),
            createGravity(config.getDefaultGravity()),
            new GridCollisionComponent(),
            entityCollision,
            new OverlapAttackableComponent(),
            new ColliderComponent(),
            new BlockComponent(),
            new MountableComponent(),
            new HealthComponent(1f, 0.66f),
            new MiniBarComponent(false),
            new WaterCollisionComponent(),
            new SplashComponent(),
            new PlayerComponent(),
            createAnimation("player_idle", direction.equals("left"))
        );
        return result;
    }

    public Entity createTequila(Parameters parameters) {
        return createItem(parameters);
    }

    public Entity createLollipop(Parameters parameters) {
        return createItem(parameters);
    }

    public Entity createCoin(Parameters parameters) {
        return createItem(parameters);
    }

    public Entity createFly(Parameters parameters) {
        return createFly(parameters, FlyType.DEFAULT);
    }

    private Entity createFly(Parameters parameters, FlyType flyType) {
        Entity result = new Entity(engine);
        result.addComponents(
            createBody(parameters, 28, 14),
            new VelocityComponent(),
            new GridCollisionComponent(),
            new EntityCollisionComponent(PlayerComponent.class),
            new BlockComponent(),
            new EnemyComponent("fly"),
            new ActivateOnScreenComponent(),
            new HealthComponent(1.0f),
            //new MiniBarComponent(true),
            new OverlapAttackComponent(flyType.getPower(), true),
            new OverlapAttackableComponent(),
            /*
            new WalkerComponent(
                parameters.get("direction", "left"),
                parameters.getFloat("speed", flyType.getDefaultSpeed()),
                parameters.getBoolean("watch_edge", true),
                false, true
            ),
             */
            createAnimation(flyType.getAnimPrefix() + "fly_idle")
        );
        addMountableIfNeeded(result, parameters);
        return result;
    }

    public Entity createBeaver(Parameters parameters) {
        Entity result = new Entity(engine);
        result.addComponents(
            createBody(parameters, 11, 23),
            new OverlapAttackableComponent(),
            createGravity(config.getDefaultGravity()),
            new GridCollisionComponent(),
            new EntityCollisionComponent(),
            new BlockComponent(),
            new ActivateOnScreenComponent(),
            new HealthComponent(1.0f),
            new EnemyComponent("enemy_beaver"),
            new MiniBarComponent(true),
            new OverlapAttackComponent(.25f, true),
            new WalkerComponent(
                parameters.get("direction", "left"),
                parameters.getFloat("speed", 40),
                parameters.getBoolean("watch_edge", true),
                true, true
            ),
            createAnimation("enemy_beaver_walk")
        );
        addMountableIfNeeded(result, parameters);
        return result;
    }

    public Entity createHedgehog(Parameters parameters) {
        Entity result = new Entity(engine);
        result.addComponents(
            createBody(parameters, 20, 12),
            createGravity(config.getDefaultGravity()),
            new OverlapAttackableComponent(),
            new GridCollisionComponent(),
            new EntityCollisionComponent(),
            new BlockComponent(),
            new ActivateOnScreenComponent(),
            new HealthComponent(1.0f),
            new EnemyComponent("enemy_hedgehog"),
            new MiniBarComponent(true),
            new OverlapAttackComponent(.33f, true),
            new WalkerComponent(
                parameters.get("direction", "left"),
                parameters.getFloat("speed", 20),
                parameters.getBoolean("watch_edge", true),
                true, false
            ),
            createAnimation("enemy_hedgehog"),
            new RushComponent() // after View added!
        );
        addMountableIfNeeded(result, parameters);
        return result;
    }

    public Entity createFrog(Parameters parameters) {
        return createFrog(parameters, FrogType.GREEN);
    }

    public Entity createPurpleFrog(Parameters parameters) {
        return createFrog(parameters, FrogType.PURPLE);
    }

    private Entity createFrog(Parameters parameters, FrogType frogType) {
        Entity result = new Entity(engine);
        result.addComponents(
            createBody(parameters, 20, 15),
            createAnimation(frogType.getAnimPrefix() + "frog_idle"),
            new OverlapAttackableComponent(),
            createGravity(config.getDefaultGravity()),
            new ActivateOnScreenComponent(),
            new GridCollisionComponent(),
            new EntityCollisionComponent(),
            new BlockComponent(),
            new FrogComponent(
                parameters.getFloat("jump_speed", FrogComponent.DEFAULT_JUMP_VELOCITY),
                parameters.getFloat("forward_speed", FrogComponent.DEFAULT_FORWARD_VELOCITY),
                parameters.getFloat("wait", FrogComponent.DEFAULT_WAIT_TO_JUMP),
                parameters.get("direction", FrogComponent.DEFAULT_DIRECTION),
                frogType
            ),
            new HealthComponent(1.0f),
            new EnemyComponent("enemy_" + frogType.getAnimPrefix() + "frog"),
            new MiniBarComponent(true),
            new OverlapAttackComponent(frogType.getPower(), true),
            new WaterCollisionComponent(),
            new SplashComponent()
        );
        addMountableIfNeeded(result, parameters);
        return result;
    }

    private void addMountableIfNeeded(Entity entity, Parameters parameters) {
        if (parameters.getBoolean("mountable", false)) {
            entity.addComponent(new MountableComponent());
        }
    }

    public Entity createPiranha(Parameters parameters) {
        Entity result = new Entity(engine);
        result.addComponents(
            createBody(parameters, 20, 28),
            new VelocityComponent(),
            new JumperComponent(
                parameters.getFloat("start_speed", 420.0f),
                parameters.getFloat("gravity", config.getDefaultGravity()),
                parameters.getFloat("wait", 1.0f)
            ),
            new BlockComponent(),
            new ActivateOnScreenComponent(),
            new HealthComponent(1.0f),
            new EnemyComponent("enemy_piranha"),
            new MiniBarComponent(true),
            new OverlapAttackableComponent(),
            new OverlapAttackComponent(0.33f, true),
            new WaterCollisionComponent(),
            new SplashComponent(),
            createAnimation("piranha")
        );
        return result;
    }


    public Entity createPurplePiranha(Parameters parameters) {
        Entity result = new Entity(engine);
        result.addComponents(
            createBody(parameters, 28, 20, Align.CENTER),
            createAnimation("purple_piranha", Align.CENTER),
            new VelocityComponent(),
            new MovableComponent(
                parameters.get("path"),
                parameters.getFloat("speed", 60.0f),
                parameters.getBoolean("slowing", true),
                parameters.getInteger("start_index", 0),
                parameters.get("move_type", MovableComponent.FORWARD_CONTINUOUS),
                parameters.getFloat("wait_time", 0),
                true,
                true
            ),
            new BlockComponent(),
            new ActivateOnScreenComponent(),
            new OverlapAttackableComponent(),
            new OverlapAttackComponent(
                parameters.getFloat("power", 0.25f),
                true
            ),
            new HealthComponent(1.0f),
            new EnemyComponent("enemy_purple_piranha"),
            new MiniBarComponent(true)
        );
        return result;
    }

    public Entity createWatermine(Parameters parameters) {
        Entity result = new Entity(engine);
        result.addComponents(
            createBody(parameters, 26, 26, Align.CENTER),
            createSprite("water_mine", Align.CENTER),
            new VelocityComponent(),
            new MovableComponent(
                parameters.get("path"),
                parameters.getFloat("speed", 60.0f),
                parameters.getBoolean("slowing", true),
                parameters.getInteger("start_index", 0),
                parameters.get("move_type", MovableComponent.FORWARD_CONTINUOUS),
                parameters.getFloat("wait_time", 0),
                false,
                true
            ),
            new BlockComponent(),
            new ActivateOnScreenComponent(),
            new OverlapAttackableComponent(), // because the bullets need to collide with this
            new OverlapAttackComponent(
                parameters.getFloat("power", 0.5f),
                true
            )
        );
        return result;
    }

    public Entity createCircularSaw(Parameters parameters) {
        Entity result = new Entity(engine);
        result.addComponents(
            createBody(parameters, 36, 36, Align.CENTER),
            createAnimation("circular_saw", Align.CENTER),
            new VelocityComponent(),
            new MovableComponent(
                parameters.get("path"),
                parameters.getFloat("speed", 60.0f),
                parameters.getBoolean("slowing", true),
                parameters.getInteger("start_index", 0),
                parameters.get("move_type", MovableComponent.FORWARD_CONTINUOUS),
                parameters.getFloat("wait_time", 0),
                false,
                true
            ),
            new BlockComponent(),
            new ActivateOnScreenComponent(),
            new OverlapAttackableComponent(),
            new OverlapAttackComponent(
                parameters.getFloat("power", 0.5f),
                true
            )
        );
        // TODO: add more entities (bodies) because of the circular shape
        return result;
    }

    public Entity createCoconut(Parameters parameters) {
        Entity result = new Entity(engine);
        result.addComponents(
            createBody(parameters, 12, 17),
            createSprite("coconut"),
            new VelocityComponent(),
            new GridCollisionComponent(),
            new BlockComponent(),
            new OverlapAttackableComponent(),
            new OverlapAttackComponent(0.5f, true),
            new CrushComponent("coconut", "crush"),
            new FallingComponent(config.getDefaultGravity(),0.33f, null, false),
            new ActivateOnScreenComponent(),
            new StartFallingInDistanceComponent(
                parameters.getFloat("distance", 64f)
            )
        );
        return result;
    }


    public Entity createTram(Parameters parameters) {

        parameters.set("active", true);

        Entity result = new Entity(engine);
        result.addComponents(
            createBody(parameters, 24, 12),
            createSprite("tram_wheel"),
            createGravity(config.getDefaultGravity()),
            new GridCollisionComponent(),
            new OverlapAttackableComponent(),
            new TramComponent(parameters.getFloat("speed", 160)),
            new ColliderComponent(),
            new BlockComponent(),
            new EntityCollisionComponent(),
            new PlatformComponent()
        );

        Entity side = new Entity(engine);
        side.addComponents(
            createBody(4, 7, -10, 12),
            new VelocityComponent(),
            new ColliderComponent(),
            new BlockComponent(),
            new PlatformComponent(),
            new PusherComponent()
        );
        side.setParent(result);
        entityManager.addToLists(side);
        side.postConstruct();

        side = new Entity(engine);
        side.addComponents(
            createBody(4, 7, 10, 12),
            new VelocityComponent(),
            new ColliderComponent(),
            new BlockComponent(),
            new PlatformComponent(),
            new PusherComponent()
        );
        side.setParent(result);
        entityManager.addToLists(side);
        side.postConstruct();

        return result;
    }

    public Entity createRailEnd(Parameters parameters) {
        Entity railEnd = new Entity(engine);
        railEnd.addComponents(
            createBody(parameters),
            new RailEndComponent()
        );
        return railEnd;
    }

    public Entity createStamper(Parameters parameters) {
        Entity result = new Entity(engine);
        result.addComponents(
            createBody(parameters, 24, 9),
            new VelocityComponent(),
            new ColliderComponent(),
            new BlockComponent(),
            new PusherComponent(),
            createSprite("stamper_bottom")
        );
        return result;
    }

    public Entity createElectricSpike(Parameters parameters) {

        parameters.set("active", true);

        System.out.println(parameters);

        float w = parameters.getFloat("width");
        float h = parameters.getFloat("height");
        float x = parameters.getFloat("x");
        float y = parameters.getFloat("y");

        BodyComponent spikeBody;
        ViewComponent spikeView = createAnimation("electric_spike");
        spikeView.setLayer(200);
        ViewComponent topView = createSprite("electric_top");
        topView.setLayer(200);
        BodyComponent topBody = createBody(16 , 16, 0, 0);
        ViewComponent bottomView = createSprite("electric_bottom");
        bottomView.setLayer(200);
        BodyComponent bottomBody = createBody(16 , 16, 0, 0);

        // horizontal
        if (w > h) {
            h = 12;
            y += 2;
            spikeBody = createBody(w-32, h, -w/2+16, 0, Align.LEFT_BOTTOM);

            spikeView.setAlign(0, Align.LEFT_BOTTOM);
            spikeView.setRepeatX((int)(w / 16f) - 2);
            spikeView.setOffsetY(-2);

            topBody.setX(w/2 - 8);
            topBody.setY(2);
            topBody.setHeight(8);
            topView.setOffsetY(-4);

            bottomBody.setX(-w/2 + 8);
            bottomBody.setHeight(8);
            bottomBody.setY(2);
            bottomView.setOffsetY(-4);

            // rotate the view
            GameSprite sprite = spikeView.getSprite(0);
            sprite.rotate90();
            sprite = topView.getSprite(0);
            sprite.rotate90();
            sprite = bottomView.getSprite(0);
            sprite.rotate90();
        }

        // vertical
        else {
            w = 12;
            x += 2;
            spikeBody = createBody(w, h - 32, 0, 0);
            spikeBody.setY(16);
            spikeView.setRepeatY((int)(h / 16f) - 2);
            topBody.setY(h - 16);
            topBody.setWidth(8);
            bottomBody.setWidth(8);

        }

        Entity spike = new Entity(engine);
        Entity bottom = new Entity(engine);
        Entity top = new Entity(engine);
        Entity result = new Entity(engine);

        parameters.set("x", x);
        parameters.set("y", y);
        parameters.set("width", w);
        parameters.set("height", h);
        result.addComponents(
            createBody(parameters),
            new ElectricSpikeComponent(
                spikeView,
                parameters.getFloat("inactive_time", 0.5f),
                parameters.getFloat("active_time", 0.5f),
                parameters.getBoolean("currently_active", true),
                parameters.getBoolean("start", true),
                parameters.getFloat("current_time", 0)
            ),
            new ViewComponent(engine),
            new OverlapAttackComponent(parameters.getFloat("power", 1.0f), true),
            new SwitchableComponent(parameters.get("inputs"))
        );

        spike.addComponents(spikeBody, spikeView);
        spike.setParent(result);
        entityManager.addToLists(spike);
        spike.postConstruct();

        bottom.addComponents(
            bottomBody,
            bottomView,
            new ColliderComponent(),
            new BlockComponent(),
            new OverlapAttackableComponent()
        );
        bottom.setParent(result);
        entityManager.addToLists(bottom);
        bottom.postConstruct();

        top.addComponents(
            topBody,
            topView,
            new ColliderComponent(),
            new BlockComponent(),
            new OverlapAttackableComponent()
        );
        top.setParent(result);
        entityManager.addToLists(top);
        top.postConstruct();

        return result;
    }

    public Entity createBigRock(Parameters parameters) {
        Entity bigRock = new Entity(engine);
        bigRock.addComponents(
            createBody(parameters),
            createSprite("big_rock"),
            new BlockComponent()
        );
        return bigRock;
    }

    public Entity createUfo(Parameters parameters) {
        parameters.set("active", true);

        Entity alien1 = new Entity(engine);
        ViewComponent alien1View = createAnimation("alien1");
        alien1.addComponents(
            createBody(21, 32, -7, 8),
            alien1View
        );
        Entity alien2 = new Entity(engine);
        ViewComponent alien2View = createAnimation("alien2");
        alien2.addComponents(
            createBody(21, 32, 7, 8),
            alien2View
        );
        Entity hostage = new Entity(engine);
        ViewComponent hostageView = createSprite("foxgirl");
        hostageView.flipX(true);
        hostageView.setVisible(false);
        hostage.addComponents(
            createBody(21, 32, 0, 8),
            hostageView
        );

        ViewComponent view = createAnimation("ufo");
        view.setLayer(200);

        Entity ufo = new Entity(engine);
        ufo.addComponents(
            createBody(parameters),
            createAnimation("ufo"),
            new VelocityComponent(),
            new UfoComponent(),
            new BlockComponent(),
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
            new SwitchableComponent(parameters.get("inputs"))
        );

        hostage.setParent(ufo);
        entityManager.addToLists(hostage);
        hostage.postConstruct();

        alien1.setParent(ufo);
        entityManager.addToLists(alien1);
        alien1.postConstruct();

        alien2.setParent(ufo);
        entityManager.addToLists(alien2);
        alien2.postConstruct();

        return ufo;
    }

}

