package com.graphics.camera;

public final class Camera2D {
    private static final float MIN_ZOOM = 0.1f;
    private static final float MAX_ZOOM = 5.0f;
    private static final float MIN_POSITION = -0.9f;
    private static final float MAX_POSITION = 0.9f;

    private float x;
    private float y;
    private float zoom = 1.0f;

    public void move(float deltaX, float deltaY) {
        x = clamp(x + deltaX, MIN_POSITION, MAX_POSITION);
        y = clamp(y + deltaY, MIN_POSITION, MAX_POSITION);
    }

    public void changeZoom(float amount) {
        zoom = clamp(zoom + amount, MIN_ZOOM, MAX_ZOOM);
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public float getZoom() { return zoom; }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
