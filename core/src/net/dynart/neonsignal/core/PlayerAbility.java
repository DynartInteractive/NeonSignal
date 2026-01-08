package net.dynart.neonsignal.core;

public enum PlayerAbility {

    MOVE,
    JUMP,
    DOUBLE_JUMP,
    PUNCH,
    CROWBAR_PUNCH,
    FIRE_PUNCH;

    public static final PlayerAbility[] ANY_JUMP = { PlayerAbility.JUMP, PlayerAbility.DOUBLE_JUMP };
    public static final PlayerAbility[] ANY_PUNCH = { PlayerAbility.PUNCH, PlayerAbility.CROWBAR_PUNCH, PlayerAbility.FIRE_PUNCH };
    public static final PlayerAbility[] CHANGE_WEAPON = { PlayerAbility.CROWBAR_PUNCH, PlayerAbility.FIRE_PUNCH };
}
