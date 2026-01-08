package net.dynart.neonsignal.core.utils;

public enum Align {

    LEFT_TOP(0, -2, 2, 0),
    LEFT_CENTER(0, -1, 2, 1),
    LEFT_BOTTOM(0, 0, 2, 2),
    CENTER_TOP(-1, -2, 1, 0),
    CENTER(-1, -1, 1, 1),
    CENTER_BOTTOM(-1, 0, 1, 2),
    RIGHT_TOP(-2, -2, 0, 0),
    RIGHT_CENTER(-2, -1, 0, 1),
    RIGHT_BOTTOM(-2, 0, 0, 2);

    private final float leftMultiplier;
    private final float bottomMultiplier;
    private final float rightMultiplier;
    private final float topMultiplier;

    Align(float leftMultiplier, float bottomMultiplier, float rightMultiplier, float topMultiplier) {
        this.leftMultiplier = leftMultiplier;
        this.bottomMultiplier = bottomMultiplier;
        this.rightMultiplier = rightMultiplier;
        this.topMultiplier = topMultiplier;
    }

    public float getLeftMultiplier() {
        return leftMultiplier;
    }

    public float getBottomMultiplier() {
        return bottomMultiplier;
    }

    public float getTopMultiplier() {
        return topMultiplier;
    }

    public float getRightMultiplier() {
        return rightMultiplier;
    }
}