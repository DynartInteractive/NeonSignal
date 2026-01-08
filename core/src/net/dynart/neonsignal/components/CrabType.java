package net.dynart.neonsignal.components;

public enum CrabType {

    RED("", 0.25f, 28),
    BLUE("blue_", 0.34f, 40);

    private final String animPrefix;
    private final float power;
    private final float defaultSpeed;

    CrabType(String animPrefix, float power, float defaultSpeed) {
        this.animPrefix = animPrefix;
        this.power = power;
        this.defaultSpeed = defaultSpeed;
    }

    public String getAnimPrefix() {
        return animPrefix;
    }

    public float getPower() {
        return power;
    }

    public float getDefaultSpeed() {
        return defaultSpeed;
    }
}