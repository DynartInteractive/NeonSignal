package net.dynart.neonsignal.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;

import net.dynart.neonsignal.core.Component;
import net.dynart.neonsignal.core.Entity;
import net.dynart.neonsignal.core.GameScene;
import net.dynart.neonsignal.core.MoveOnSegmentUtil;
import net.dynart.neonsignal.core.Path;
import net.dynart.neonsignal.core.PathManager;
import net.dynart.neonsignal.core.listeners.MessageListener;

public class MovableComponent extends Component {

    public static final String FORWARD_CONTINUOUS = "fc";
    public static final String FORWARD_ONCE = "fo";
    public static final String FORWARD_STOPPABLE = "fstop";
    public static final String FORWARD_STEP = "fstep";
    public static final String PING_PONG_ONCE = "po";

    private final boolean start;
    private Path path;
    private final String pathName;
    private BodyComponent body;
    private VelocityComponent velocity;
    private int segmentIndex;
    private final int startSegmentIndex;
    private final MoveOnSegmentUtil mosUtil;
    private final boolean slowing;
    private final float speed;
    private final String moveType;
    private final float waitTime;
    private float currentWaitTime;
    private final boolean flipView;
    private boolean forward = true;
    private boolean finished = false;
    private final Vector2 startPosition = new Vector2();
    private int finishIndex;
 
    public MovableComponent(String pathName, float speed, boolean slowing, int startSegmentIndex, String moveType, float waitTime, boolean flipView, boolean start) {
        this.pathName = pathName;
        this.speed = speed;
        this.slowing = slowing;
        this.startSegmentIndex = startSegmentIndex;
        this.moveType = moveType;
        this.waitTime = waitTime;
        this.flipView = flipView;
        this.start = start;
        mosUtil = new MoveOnSegmentUtil();
    }

    public void setFinishIndex(int value) {
        finishIndex = value;
    }

    public void finish() {
        body.setGlobalX(path.getX(finishIndex == -1 ? path.getLength() - 1 : finishIndex));
        body.setGlobalY(path.getY(finishIndex == -1 ? path.getLength() - 1 : finishIndex));
        velocity.setX(0);
        velocity.setY(0);
        setActive(false);
    }

    public String getMoveType() {
        return moveType;
    }

    @Override
    public void postConstruct(final Entity entity) {
        super.postConstruct(entity);

        GameScene gameScene = engine.getGameScene();
        PathManager pathManager = gameScene.getPathManager();

        body = entity.getComponent(BodyComponent.class);
        velocity = entity.getComponent(VelocityComponent.class);
        path = pathManager.get(pathName);
        startPosition.set(body.getX(), body.getY());

        setActive(start);

        messageHandler.subscribe(SwitchableComponent.ON, (sender, message) -> setActive(true));
        messageHandler.subscribe(SwitchableComponent.OFF, (sender, message) -> {
            if (getMoveType().equals(FORWARD_STOPPABLE)) {
                setActive(false);
            }
        });

        reset();
    }

    public boolean isStart() {
        return start;
    }

    @Override
    public void preUpdate(float delta) {
        if (finished) {
            return;
        }
        if (currentWaitTime > 0) {
            currentWaitTime -= delta;
            return;
        }

        if (!mosUtil.update(delta)) {

            boolean setVelocity = adjustNextDirection(true);

            // stepping
            if (moveType.equals(FORWARD_STEP)) {
                velocity.setX(0);
                velocity.setY(0);
                setActive(false);
                setVelocity = false;
            }
            //

            body.setGlobalX(mosUtil.getCurrentX());
            body.setGlobalY(mosUtil.getCurrentY());
            if (!setVelocity) {
                return;
            }
        }
        velocity.setX(mosUtil.getVelocityX());
        velocity.setY(mosUtil.getVelocityY());
        if (flipView) {
            ViewComponent view = entity.getComponent(ViewComponent.class);
            view.flipX(velocity.getX() > 0);
        }

    }

    private boolean adjustNextDirection(boolean next) {
        if (path == null) {
            return false;
        }
        boolean goesNextSegment = true;
        if (forward) {
            if (next) {
                segmentIndex++;
            }
            if (segmentIndex == path.getLength()) { // after last segment forward
                switch (moveType) {
                    case FORWARD_CONTINUOUS: // we just continue
                    case FORWARD_STOPPABLE:
                    case FORWARD_STEP:
                        segmentIndex = 0;
                        break;
                    case PING_PONG_ONCE: // set movement backward
                        forward = false;
                        segmentIndex--;
                        currentWaitTime = waitTime;
                        velocity.setX(0);
                        velocity.setY(0);
                        goesNextSegment = false;
                        break;
                    case FORWARD_ONCE:  // finish movement
                        finished = true;
                        velocity.setX(0);
                        velocity.setY(0);
                        goesNextSegment = false;
                        break;
                }
            }

        } else { // backward: only PING_PONG_ONCE
            segmentIndex--;
            if (segmentIndex == -1) { // after last segment backward
                setActive(false);
                segmentIndex = 0;
                velocity.setX(0);
                velocity.setY(0);
                forward = true;
                goesNextSegment = false;
            }
        }
        if (goesNextSegment) {
            mosUtil.init(body.getGlobalX(), body.getGlobalY(), path.getX(segmentIndex), path.getY(segmentIndex));
        }
        return goesNextSegment;
    }

    @Override
    public void setActive(boolean value) {
        if (!value && isActive() && moveType.equals(FORWARD_STOPPABLE)) {
            // switched off
            velocity.setX(0);
            velocity.setY(0);
        }
        if (value && !isActive()) {
            segmentIndex = startSegmentIndex;
            adjustNextDirection(false);
        }
        super.setActive(value);
    }

    public void reset() {
        finished = false;
        body.setGlobalX(startPosition.x);
        body.setGlobalY(startPosition.y);
        velocity.setX(0);
        velocity.setY(0);
        segmentIndex = this.startSegmentIndex - 1;
        mosUtil.setDistanceToSlow(config.getTileWidth() / 2f, false, slowing);
        mosUtil.setSpeed(speed);
    }

}
