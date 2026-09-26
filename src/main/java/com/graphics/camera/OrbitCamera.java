package com.graphics.camera;

public final class OrbitCamera {
    private float angle = 0.6f;
    private final float radius;
    private final float height;

    public OrbitCamera(float radius, float height) {
        this.radius = radius;
        this.height = height;
    }

    public void rotate(float amount) {
        angle += amount;
    }

    public float x() { return (float) Math.sin(angle) * radius; }
    public float y() { return height; }
    public float z() { return (float) Math.cos(angle) * radius; }
    public float angle() { return angle; }
}
