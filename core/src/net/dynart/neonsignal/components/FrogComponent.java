package net.dynart.neonsignal.components;

import net.dynart.neonsignal.core.Component;
import net.dynart.neonsignal.core.Entity;
import net.dynart.neonsignal.core.utils.Direction;

public class FrogComponent extends Component {

    public static final float DEFAULT_JUMP_VELOCITY = 200;
    public static final float DEFAULT_FORWARD_VELOCITY = 100;
    public static final float DEFAULT_WAIT_TO_JUMP = 0.6f;
    public static final String DEFAULT_DIRECTION = "left";

    private static final float DEFAULT_TIME_TO_JUMP = 0.4f; // depends on the animation

    private final float jumpVelocity;
    private final float waitToJump;
    private final boolean left;
    private final FrogType frogType;

    private float forwardVelocity;
    private float elapsed;
    private float timeToJump;
    private VelocityComponent velocity;
    private BodyComponent body;
    private ViewComponent view;
    private boolean jumpEnd;


    public FrogComponent(float jumpVelocity, float forwardVelocity, float waitToJump, String direction, FrogType frogType) {
        left = direction.equals("left");
        this.waitToJump = waitToJump;
        this.jumpVelocity = jumpVelocity;
        this.forwardVelocity = left ? -forwardVelocity : forwardVelocity;
        this.frogType = frogType;
    }

    @Override
    public void postConstruct(final Entity entity) {
        super.postConstruct(entity);

        velocity = entity.getComponent(VelocityComponent.class);
        view = entity.getComponent(ViewComponent.class);
        body = entity.getComponent(BodyComponent.class);

        view.flipX(!left);
    }

    private void swapForward() {
        forwardVelocity = -forwardVelocity;
        view.flipX(forwardVelocity > 0);
    }

    private void resetJump() {
        if (jumpEnd) {
            view.setAnimationTime(0, 0);
            view.setAnimation(0, frogType.getAnimPrefix() + "frog_idle");
            jumpEnd = false;
            velocity.setX(0);
        }
    }

    @Override
    public void postUpdate(float delta) {
        if (body.isSideCollided()) {
            swapForward();
        }
        if (body.isBottomCollided()) {
            resetJump();
        }
        elapsed += delta;
        if (!body.isInAir()) {

            // wait for jump
            if (timeToJump == 0 && elapsed > waitToJump) {
                timeToJump = DEFAULT_TIME_TO_JUMP;
                view.setAnimationTime(0, 0);
                view.setAnimation(0, frogType.getAnimPrefix() + "frog_jump_begin");
            }

            // jump
            if (timeToJump > 0) {
                timeToJump -= delta;
                if (timeToJump < 0) {

                    // turn to the player at jump
                    BodyComponent playerBody = engine.getGameScene().getPlayer().getComponent(BodyComponent.class);
                    if (Math.abs(body.getY() - playerBody.getY()) < 32) {
                        forwardVelocity = body.getDirection(true, playerBody) == Direction.RIGHT ? Math.abs(forwardVelocity) : -Math.abs(forwardVelocity);
                        view.flipX(forwardVelocity > 0);
                    }

                    velocity.setY(jumpVelocity);
                    timeToJump = 0;
                    elapsed = 0;
                }
            }
        } else {

            // set X velocity, and switch between jump begin/end animation
            velocity.setX(forwardVelocity);
            if (velocity.getGlobalY() < 0 && !jumpEnd) {
                view.setAnimationTime(0, 0);
                view.setAnimation(0, frogType.getAnimPrefix() + "frog_jump_end");
                jumpEnd = true;
            }
        }
    }


}
