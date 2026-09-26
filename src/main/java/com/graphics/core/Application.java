package com.graphics.core;

import org.lwjgl.glfw.GLFW;

/** Ciclo de vida común para las aplicaciones OpenGL del proyecto. */
public abstract class Application {
    protected Window window;

    public final void run() {
        try {
            initialize();
            loop();
        } finally {
            destroy();
        }
    }

    protected abstract void initialize();
    protected abstract void update(float deltaTime);
    protected abstract void render();
    protected abstract void destroy();

    private void loop() {
        float previousTime = (float) GLFW.glfwGetTime();

        while (!window.shouldClose()) {
            float currentTime = (float) GLFW.glfwGetTime();
            float deltaTime = currentTime - previousTime;
            previousTime = currentTime;

            update(deltaTime);
            render();
            window.swapBuffers();
            window.pollEvents();
        }
    }
}
