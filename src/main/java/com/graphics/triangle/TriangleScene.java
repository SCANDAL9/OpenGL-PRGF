package com.graphics.triangle;

import com.graphics.camera.Camera2D;
import com.graphics.input.Input;

public final class TriangleScene {
    private static final float MOVE_SPEED = 1.2f;
    private static final float KEYBOARD_ZOOM_SPEED = 1.0f;
    private static final float SCROLL_ZOOM_SPEED = 0.1f;

    private final Camera2D camera;
    private final Input input;

    public TriangleScene(Camera2D camera, Input input) {
        this.camera = camera;
        this.input = input;
    }

    public void update(float deltaTime) {
        float movement = MOVE_SPEED * deltaTime;
        float zoom = KEYBOARD_ZOOM_SPEED * deltaTime;

        if (input.isLeftPressed()) camera.move(-movement, 0);
        if (input.isRightPressed()) camera.move(movement, 0);
        if (input.isUpPressed()) camera.move(0, movement);
        if (input.isDownPressed()) camera.move(0, -movement);
        if (input.isZoomInPressed()) camera.changeZoom(zoom);
        if (input.isZoomOutPressed()) camera.changeZoom(-zoom);

        camera.changeZoom(input.consumeScrollDelta() * SCROLL_ZOOM_SPEED);
    }

    public Camera2D camera() { return camera; }
}
