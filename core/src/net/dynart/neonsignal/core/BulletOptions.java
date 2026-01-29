package net.dynart.neonsignal.core;

import net.dynart.neonsignal.components.BodyComponent;

public class BulletOptions {
    public BodyComponent ownerBody;
    public Float positionX = null;
    public Float positionY = null;
    public float velocityX;
    public float velocityY;
    public boolean flipX = false;
    public boolean flipY = false;
    public boolean collideWithGrid = true;
    public float lifeTime = -1;
    public float power = 1.0f;
    public float rotation = 0;
    public boolean enemyFire = false;
    public Float sizeX = null;
    public Float sizeY = null;
    public String sprite = null;
    public boolean explosive = false;
    public String sparkEffect = null;

    public BulletOptions() {}

    public BulletOptions(BodyComponent ownerBody,
        float velocityX,
        float velocityY,
        boolean flipX,
        boolean flipY,
        boolean collideWithGrid,
        float lifeTime,
        float rotation,
        boolean enemyFire
    ) {
        this.ownerBody = ownerBody;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.flipX = flipX;
        this.flipY = flipY;
        this.collideWithGrid = collideWithGrid;
        this.lifeTime = lifeTime;
        this.rotation = rotation;
        this.enemyFire = enemyFire;
    }
}
