package net.dynart.neonsignal.core;

public class Path {

    float[] vertices;

    public Path(float[] vertices) {
        this.vertices = vertices;
    }

    public float getX(int index) {
        int realIndex = index * 2;
        if (realIndex >= vertices.length) {
            return 0; // TODO: log.warn
        }
        return (int)vertices[realIndex];
    }

    public float getY(int index) {
        int realIndex = index * 2 + 1;
        if (realIndex >= vertices.length) {
            return 0; // TODO: log.warn
        }
        return (int)vertices[realIndex];
    }

    public int getLength() {
        return vertices.length / 2;
    }

}
