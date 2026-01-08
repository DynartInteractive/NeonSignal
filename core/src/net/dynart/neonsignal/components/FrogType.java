package net.dynart.neonsignal.components;

public enum FrogType {

    GREEN("", 0.25f),
    PURPLE("purple_", 0.34f);

    private final String animPrefix;
    private final float power;

    FrogType(String animPrefix, float power) {
        this.animPrefix = animPrefix;
        this.power = power;
    }

    public String getAnimPrefix() {
        return animPrefix;
    }

    public float getPower() {
        return power;
    }
}
