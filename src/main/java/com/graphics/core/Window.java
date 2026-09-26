package com.graphics.core;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

public final class Window {
    private final long handle;

    public Window(int width, int height, String title) {
        if (!GLFW.glfwInit()) throw new IllegalStateException("No se pudo inicializar GLFW");
        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);

        handle = GLFW.glfwCreateWindow(width, height, title, 0, 0);
        if (handle == 0) throw new IllegalStateException("No se pudo crear la ventana");

        GLFW.glfwMakeContextCurrent(handle);
        GLFW.glfwSwapInterval(1);
        GL.createCapabilities();
        GLFW.glfwSetFramebufferSizeCallback(handle, (ignored, newWidth, newHeight) ->
            GL11.glViewport(0, 0, newWidth, newHeight));
        GLFW.glfwShowWindow(handle);
    }

    public long handle() { return handle; }
    public boolean shouldClose() { return GLFW.glfwWindowShouldClose(handle); }
    public void swapBuffers() { GLFW.glfwSwapBuffers(handle); }
    public void pollEvents() { GLFW.glfwPollEvents(); }

    public void destroy() {
        GLFW.glfwDestroyWindow(handle);
        GLFW.glfwTerminate();
    }
}
