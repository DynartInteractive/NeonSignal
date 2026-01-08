package net.dynart.neonsignal.core.utils;

import net.dynart.neonsignal.components.BodyComponent;
import net.dynart.neonsignal.core.EntityManager;

public class AreaPosition {

    private int left;
    private int right;
    private int bottom;
    private int top;

    public AreaPosition() {}

    public void setByBodyVerticalPosition(BodyComponent body) {
        top = (int)body.getTop() / EntityManager.AREA_SIZE;
        bottom = (int)body.getBottom() / EntityManager.AREA_SIZE;
    }

    public void setByBodyHorizontalPosition(BodyComponent body) {
        left = (int)body.getLeft() / EntityManager.AREA_SIZE;
        right = (int)body.getRight() / EntityManager.AREA_SIZE;
    }

    public void setByBodyLastPosition(BodyComponent body) {
        left = (int)body.getLastLeft() / EntityManager.AREA_SIZE;
        top = (int)body.getLastTop() / EntityManager.AREA_SIZE;
        right = (int)body.getLastRight() / EntityManager.AREA_SIZE;
        bottom = (int)body.getLastBottom() / EntityManager.AREA_SIZE;
    }

    public int getLeft() {
        return left;
    }

    public int getRight() {
        return right;
    }

    public int getBottom() {
        return bottom;
    }

    public int getTop() {
        return top;
    }

    public boolean equals(AreaPosition other) {
        return left == other.left && right == other.right && top == other.top && bottom == other.bottom;
    }
}
