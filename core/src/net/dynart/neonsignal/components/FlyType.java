package net.dynart.neonsignal.components;

public enum FlyType {

    DEFAULT("", 0.25f, 28);

    private final String animPrefix;
    private final float power;
    private final float defaultSpeed;

    FlyType(String animPrefix, float power, float defaultSpeed) {
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