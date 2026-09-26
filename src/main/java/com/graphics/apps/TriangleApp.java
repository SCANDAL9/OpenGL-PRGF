package com.graphics.apps;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import com.graphics.camera.Camera2D;
import com.graphics.core.Application;
import com.graphics.core.Window;
import com.graphics.graphics.Mesh;
import com.graphics.graphics.ShaderProgram;
import com.graphics.input.Input;
import com.graphics.triangle.TriangleScene;

/** Aplicación del triángulo: movimiento con teclado y zoom con teclado/rueda. */
public final class TriangleApp extends Application {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;


    private Mesh triangle;
    private ShaderProgram shader;
    private TriangleScene scene;

    @Override
    protected void initialize() {
        window = new Window(WIDTH, HEIGHT, "OpenGL - Triangle");
        Input input = new Input(window.handle());
        GLFW.glfwSetKeyCallback(window.handle(), (handle, key, scancode, action, mods) -> {
            if (key == GLFW.GLFW_KEY_ESCAPE && action == GLFW.GLFW_PRESS) {
                GLFW.glfwSetWindowShouldClose(handle, true);
            }
        });

        scene = new TriangleScene(new Camera2D(), input);
        triangle = new Mesh(new float[] {
             0.0f,  0.5f, 0.0f,
            -0.5f, -0.5f, 0.0f,
             0.5f, -0.5f, 0.0f
        });
        shader = new ShaderProgram(VERTEX_SHADER, FRAGMENT_SHADER);

        int[] framebufferWidth = new int[1];
        int[] framebufferHeight = new int[1];
        GLFW.glfwGetFramebufferSize(window.handle(), framebufferWidth, framebufferHeight);
        GL11.glViewport(0, 0, framebufferWidth[0], framebufferHeight[0]);
    }

    @Override
    protected void update(float deltaTime) {
        scene.update(deltaTime);
    }

    @Override
    protected void render() {
        GL11.glClearColor(0.08f, 0.08f, 0.12f, 1.0f);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

        shader.use();
        Camera2D camera = scene.camera();
        shader.setVec2("uOffset", camera.getX(), camera.getY());
        shader.setFloat("uZoom", camera.getZoom());
        triangle.render();
    }

    @Override
    protected void destroy() {
        if (triangle != null) triangle.destroy();
        if (shader != null) shader.destroy();
        if (window != null) window.destroy();
    }

    private static final String VERTEX_SHADER = """
        #version 330 core
        layout (location = 0) in vec3 aPos;
        uniform vec2 uOffset;
        uniform float uZoom;

        void main() {
            vec2 position = aPos.xy * uZoom + uOffset;
            gl_Position = vec4(position, aPos.z, 1.0);
        }
        """;

    private static final String FRAGMENT_SHADER = """
        #version 330 core
        out vec4 fragColor;

        void main() {
            fragColor = vec4(0.2, 0.8, 0.4, 1.0);
        }
        """;

    public static void main(String[] args) {
        new TriangleApp().run();
    }
}
