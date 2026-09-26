package com.graphics.input;

import org.lwjgl.glfw.GLFW;

public final class Input {
    private final long window;
    private float scrollDelta;

    public Input(long window) {
        this.window = window;
        GLFW.glfwSetScrollCallback(window, (ignored, xOffset, yOffset) -> scrollDelta += (float) yOffset);
    }

    public boolean isPressed(int key) {
        return GLFW.glfwGetKey(window, key) == GLFW.GLFW_PRESS;
    }

    public boolean isEscapePressed() { return isPressed(GLFW.GLFW_KEY_ESCAPE); }
    public boolean isLeftPressed() { return isPressed(GLFW.GLFW_KEY_LEFT) || isPressed(GLFW.GLFW_KEY_A); }
    public boolean isRightPressed() { return isPressed(GLFW.GLFW_KEY_RIGHT) || isPressed(GLFW.GLFW_KEY_D); }
    public boolean isUpPressed() { return isPressed(GLFW.GLFW_KEY_UP) || isPressed(GLFW.GLFW_KEY_W); }
    public boolean isDownPressed() { return isPressed(GLFW.GLFW_KEY_DOWN) || isPressed(GLFW.GLFW_KEY_S); }
    public boolean isZoomInPressed() { return isPressed(GLFW.GLFW_KEY_KP_ADD) || isPressed(GLFW.GLFW_KEY_EQUAL); }
    public boolean isZoomOutPressed() { return isPressed(GLFW.GLFW_KEY_KP_SUBTRACT) || isPressed(GLFW.GLFW_KEY_MINUS); }

    public float consumeScrollDelta() {
        float result = scrollDelta;
        scrollDelta = 0.0f;
        return result;
    }
}
