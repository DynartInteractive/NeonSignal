package net.dynart.neonsignal.core;

public enum PlayerAbility {

    MOVE,
    JUMP,
    DOUBLE_JUMP,
    DASH,
    FIRE;

    public static final PlayerAbility[] JUMPING = { PlayerAbility.JUMP, PlayerAbility.DOUBLE_JUMP };
    public static final PlayerAbility[] FIRING = { PlayerAbility.FIRE };
    public static final PlayerAbility[] DASHING = { PlayerAbility.DASH };
    public static final PlayerAbility[] CHANGE_WEAPON = { };
}
