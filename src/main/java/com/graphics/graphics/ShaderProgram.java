package com.graphics.graphics;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

public final class ShaderProgram {
    private final int id;

    public ShaderProgram(String vertexSource, String fragmentSource) {
        int vertexShader = compile(GL20.GL_VERTEX_SHADER, vertexSource);
        int fragmentShader = compile(GL20.GL_FRAGMENT_SHADER, fragmentSource);

        id = GL20.glCreateProgram();
        GL20.glAttachShader(id, vertexShader);
        GL20.glAttachShader(id, fragmentShader);
        GL20.glLinkProgram(id);

        if (GL20.glGetProgrami(id, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
            throw new IllegalStateException("No se pudo enlazar el programa: " + GL20.glGetProgramInfoLog(id));
        }

        GL20.glDeleteShader(vertexShader);
        GL20.glDeleteShader(fragmentShader);
    }

    private static int compile(int type, String source) {
        int shader = GL20.glCreateShader(type);
        GL20.glShaderSource(shader, source);
        GL20.glCompileShader(shader);

        if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            throw new IllegalStateException("No se pudo compilar el shader: " + GL20.glGetShaderInfoLog(shader));
        }
        return shader;
    }

    public void use() { GL20.glUseProgram(id); }

    public void setVec2(String name, float x, float y) {
        GL20.glUniform2f(GL20.glGetUniformLocation(id, name), x, y);
    }

    public void setFloat(String name, float value) {
        GL20.glUniform1f(GL20.glGetUniformLocation(id, name), value);
    }

    public void destroy() { GL20.glDeleteProgram(id); }
}
