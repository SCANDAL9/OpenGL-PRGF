package com.graphics.apps;

import java.util.HashMap;
import java.util.Map;

import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;


import com.graphics.camera.OrbitCamera;
import com.graphics.city.CityMap;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33.*;

public class CityApp {
    protected long ventana;
    protected int programa;
    protected int vao;
    protected int vbo;
    protected int ancho = 1100;
    protected int alto = 760;

    protected final OrbitCamera camera = new OrbitCamera(65.0f, 55.0f);
    protected final CityMap map = new CityMap();
    protected final Map<String, Integer> uniforms = new HashMap<>();

    // Cambiar el ángulo o la vista del mapa.
    private boolean vistaMapa = false;

    protected static final float LIMITE = 35;
    protected static final float CELDA = 10;

    public void run() {
        // errores en consola
        GLFWErrorCallback errores = GLFWErrorCallback.createPrint(System.err);
        // instanciamos manejador de errores
        errores.set();

        try {
            init();
            loop();
        } finally {
            limpiar();
            glfwSetErrorCallback(null);
            errores.free();
        }
    }

    private void init() {
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Failed to initialize GLFW");
        }

        //Reset options
        GLFW.glfwDefaultWindowHints();

        //Mostrar ventana solo cuando OpenGL este listo
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);

        //Redimensionar pantalla
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);

        //OpenGL 3.3
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);

        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);

        // Ventana: ancho, alto, titulo, monitor(0=normal), window share
        ventana = GLFW.glfwCreateWindow(ancho, alto, "OpenGL - City", 0, 0);

        if (ventana == 0) {
            int error = GLFW.glfwGetError(null);
            throw new IllegalStateException("Failed to create GLFW window. Error code: " + error);
        }

        //GLFW.glfwSetScrollCallback(ventana, (w, xoffset, yoffset) -> {
        //    zoom += (float) yoffset * 0.10f;
        //    zoom = clamp(zoom, ZOOM_MIN, ZOOM_MAX);
        //});


        // Operaciones exec on this windows
        GLFW.glfwMakeContextCurrent(ventana);

        // evitar el thearing?
        GLFW.glfwSwapInterval(1);

        // hacer visible
        GLFW.glfwShowWindow(ventana);

        // Enlazar las llamadas de Java con la GPU
        GL.createCapabilities();
        glEnable(GL_DEPTH_TEST);

        glfwSetFramebufferSizeCallback(ventana, (ventanaEvent, nuevoAncho, nuevoAlto) -> {
            ancho = nuevoAncho;
            alto = nuevoAlto;
            glViewport(0, 0, nuevoAncho, nuevoAlto);
        });

        int[] anchoInicial = new int[1];
        int[] altoInicial = new int[1];
        glfwGetFramebufferSize(ventana, anchoInicial, altoInicial);
        ancho = anchoInicial[0];
        alto = altoInicial[0];
        glViewport(0, 0, ancho, alto);

        glfwSetKeyCallback(ventana, (ventanaEvent, key, scancode, action, mods) -> {
            if (action == GLFW_PRESS) {
                tecla(key);
            }
        });

        creatPrograma();
        crearCubo();
    }

    private void loop() {
        double tiempoAnt = glfwGetTime();
        int cuadros = 0;
        // 0 permite jugar
        int maxCuadros = Integer.getInteger("demo.frames", 0);

        while (!glfwWindowShouldClose(ventana)) {
            // Procesar eventos de hw
            glfwPollEvents();

            double tiempoActual = glfwGetTime();
            float deltaTime = (float) Math.min(tiempoActual - tiempoAnt, 0.05);
            tiempoAnt = tiempoActual;
            actualizar(deltaTime);
            if (ancho > 0 && alto > 0) {
                dibujarFrame();
            }

            glfwSwapBuffers(ventana);
            cuadros++;
            if (maxCuadros > 0 && cuadros >=maxCuadros) {
                glfwSetWindowShouldClose(ventana, true);
            }
        }
    }

    protected void tecla(int key) {
        if (key == GLFW_KEY_ESCAPE) {
            glfwSetWindowShouldClose(ventana, true);
        }
        if (key == GLFW_KEY_M) {
            vistaMapa = !vistaMapa;
        }
    }

    private void limpiar() {
        if (programa != 0) glDeleteProgram(programa);

        if (vbo != 0) glDeleteBuffers(vbo);

        if (vao != 0) glDeleteVertexArrays(vao);

        if (ventana != 0)  {
            Callbacks.glfwFreeCallbacks(ventana);
            glfwDestroyWindow(ventana);
        }
    }

    protected boolean pulsada(int key) {
        return glfwGetKey(ventana, key) == GLFW_PRESS;
    }

    protected void actualizar(float deltaTime) {
        if (pulsada(GLFW_KEY_LEFT)) camera.rotate(-deltaTime);
        if (pulsada(GLFW_KEY_RIGHT)) camera.rotate(deltaTime);
    }

    protected void configurarCamara() {
        // 65 es el radio del circulo/orbita
        vector("uOjo", camera.x(), camera.y(), camera.z()); // camara.y() == 55?
        vector("uObjetivo", 0, 0, 0);
        decimal("uAspecto", (float) ancho / alto);
    }

    private void creatPrograma() {
        int vertex = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
        GL20.glShaderSource(vertex, vertexShader());
        GL20.glCompileShader(vertex);
        comprobarShader(vertex, "Vertex");

        int fragment = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
        GL20.glShaderSource(fragment, fragmentShader());
        GL20.glCompileShader(fragment);
        comprobarShader(fragment, "Fragment");

        programa = GL20.glCreateProgram();
        GL20.glAttachShader(programa, vertex);
        GL20.glAttachShader(programa, fragment);
        GL20.glLinkProgram(programa);

        if (GL20.glGetProgrami(programa, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
            throw new IllegalStateException("No se pudo enlazar el programa de ciudad: " +
                GL20.glGetProgramInfoLog(programa));
        }

        GL20.glDeleteShader(vertex);
        GL20.glDeleteShader(fragment);
    }

    private void comprobarShader(int shader, String tipo) {
        if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            throw new IllegalStateException(tipo + " shader de ciudad: " +
                GL20.glGetShaderInfoLog(shader));
        }
    }

    private void crearCubo() {
        float[] vertices = crearVerticesCubo();
        vao = GL30.glGenVertexArrays();
        vbo = GL15.glGenBuffers();

        GL30.glBindVertexArray(vao);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertices, GL15.GL_STATIC_DRAW);

        int stride = 6 * Float.BYTES;
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, stride, 0);
        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, stride, 3L * Float.BYTES);
        GL20.glEnableVertexAttribArray(1);

        GL30.glBindVertexArray(0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    private float[] crearVerticesCubo() {
        return new float[] {
            // Frente
            -0.5f, -0.5f,  0.5f, 0, 0, 1,
             0.5f, -0.5f,  0.5f, 0, 0, 1,
             0.5f,  0.5f,  0.5f, 0, 0, 1,
             0.5f,  0.5f,  0.5f, 0, 0, 1,
            -0.5f,  0.5f,  0.5f, 0, 0, 1,
            -0.5f, -0.5f,  0.5f, 0, 0, 1,
            // Atrás
             0.5f, -0.5f, -0.5f, 0, 0, -1,
            -0.5f, -0.5f, -0.5f, 0, 0, -1,
            -0.5f,  0.5f, -0.5f, 0, 0, -1,
            -0.5f,  0.5f, -0.5f, 0, 0, -1,
             0.5f,  0.5f, -0.5f, 0, 0, -1,
             0.5f, -0.5f, -0.5f, 0, 0, -1,
            // Izquierda
            -0.5f, -0.5f, -0.5f, -1, 0, 0,
            -0.5f, -0.5f,  0.5f, -1, 0, 0,
            -0.5f,  0.5f,  0.5f, -1, 0, 0,
            -0.5f,  0.5f,  0.5f, -1, 0, 0,
            -0.5f,  0.5f, -0.5f, -1, 0, 0,
            -0.5f, -0.5f, -0.5f, -1, 0, 0,
            // Derecha
             0.5f, -0.5f,  0.5f, 1, 0, 0,
             0.5f, -0.5f, -0.5f, 1, 0, 0,
             0.5f,  0.5f, -0.5f, 1, 0, 0,
             0.5f,  0.5f, -0.5f, 1, 0, 0,
             0.5f,  0.5f,  0.5f, 1, 0, 0,
             0.5f, -0.5f,  0.5f, 1, 0, 0,
            // Arriba
            -0.5f,  0.5f,  0.5f, 0, 1, 0,
             0.5f,  0.5f,  0.5f, 0, 1, 0,
             0.5f,  0.5f, -0.5f, 0, 1, 0,
             0.5f,  0.5f, -0.5f, 0, 1, 0,
            -0.5f,  0.5f, -0.5f, 0, 1, 0,
            -0.5f,  0.5f,  0.5f, 0, 1, 0,
            // Abajo
            -0.5f, -0.5f, -0.5f, 0, -1, 0,
             0.5f, -0.5f, -0.5f, 0, -1, 0,
             0.5f, -0.5f,  0.5f, 0, -1, 0,
             0.5f, -0.5f,  0.5f, 0, -1, 0,
            -0.5f, -0.5f,  0.5f, 0, -1, 0,
            -0.5f, -0.5f, -0.5f, 0, -1, 0
        };
    }

    protected void prepararLuces() {
        // Enviar luces al gpu
    }

    // ============== Ciudad a partir de la matriz ====================
    protected float centro(int indice) {
        return -LIMITE + CELDA * (indice + 0.5f);
    }

    protected void dibujarFrame() {
        glViewport(0, 0, ancho, alto);
        glClearColor(0.12f, 0.20f, 0.30f, 1);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glUseProgram(programa);
        glBindVertexArray(vao);
        entero("uMapa", vistaMapa ? 1 : 0);
        configurarCamara();
        prepararLuces();
        escena();

        if (glGetError() != GL_NO_ERROR) {
            throw new IllegalStateException("Error: OpenGL al dibujar");
        }
    }

    protected void escena() {
        caja(0.0f, -0.25f, 0.0f, 70.0f, 0.5f, 70.0f, 0.16f, 0.19f, 0.23f);
        for (int fila = 0; fila < map.rows(); fila++) {
            for (int columna = 0; columna < map.columns(); columna++) {
                float x = centro(columna);
                float z = centro(fila);
                int tipo = map.valueAt(fila, columna);
                if (tipo == 0) {
                    dibujarMarcasCalle(fila, columna, x, z);
                } else {
                    caja(x, 0.15f, z, 10, 0.3f, 10, 0.60f, 0.64f, 0.66f);
                    if (tipo == 1) {
                        float altura = 5 + (fila * 3 + columna * 7) % 9;
                        float rojo = 0.28f + columna * 0.045f;
                        float azul = 0.48f + fila * 0.025f;
                        caja(x, altura / 2 + 0.3f, z, 7, altura, 7, rojo, 0.40f, azul);
                        caja(x, altura + 0.45f, z, 7.3f, 0.3f, 7.03f, 0.20f, 0.26f, 0.32f);
                    } else {
                        caja(x, 0.32f, z, 9, 0.1f, 9, 0.20f, 0.45f, 0.28f);
                    }
                }
            }
        }
    }

    private void dibujarMarcasCalle(int fila, int columna, float x, float z) {
        if (fila % 2 == 0 && columna % 2 == 1) {
            for (int desplazamiento = -3; desplazamiento <= 3; desplazamiento++) {
                caja(x + desplazamiento, 0.025f, z, 1.6f, 0.03f, 0.13f, 1, 0.84f, 0.35f);
            }
        }

        if (columna % 2 == 0 && fila % 2 == 1) {
            for (int desplazamiento = -3; desplazamiento <= 3; desplazamiento++) {
                caja(x, 0.025f, z + desplazamiento, 0.13f, 0.03f, 0.13f, 1, 0.84f, 0.35f);
            }
        }
    }

    // ================ Dibujar cajas y enviar uniform =================
    protected void caja(float x, float y, float z, float sx, float sy, float sz, float r, float g, float b) {
        cajaGirada(x, y, z, sx, sy, sz, r, g, b, 0);
    }

    protected void cajaGirada(float x, float y, float z, float sx, float sy, float sz,
        float r, float g, float b, float angulo) {
            vector("uPos", x, y, z);
            vector("uEscala", sx, sy, sz);
            vector("uColor", r, g, b);
            decimal("uGiro", angulo);
            glDrawArrays(GL_TRIANGLES, 0, 36);
    }

    private int uniform(String nombre) {
        if (!uniforms.containsKey(nombre)) {
            int ubicacion = glGetUniformLocation(programa, nombre);
            uniforms.put(nombre, ubicacion);
        }
        return uniforms.get(nombre);
    }

    protected void vector(String nombre, float x, float y, float z) {
        glUniform3f(uniform(nombre), x, y, z);
    }

    protected void decimal(String nombre, float valor) {
        glUniform1f(uniform(nombre), valor);
    }

    protected void entero(String nombre, int valor) {
        glUniform1i(uniform(nombre), valor);
    }


    // ========================= Shaders =========================
    private String vertexShader() {
        return """
            #version 330 core
            layout(location = 0) in vec3 aPos; // Leemos la pos local del vertice desde el vbo
            layout(location = 1) in vec3 aNormal; // Leemos la normal de la cara desde el mismo vbo

            uniform vec3 uPos; // Centro de la caja de la ciudad
            uniform vec3 uEscala; // Tamaño de la caja en 3ra persona (cada eje)
            uniform vec3 uOjo; // Posicion de la camara u orbita
            uniform vec3 uObjetivo; // Punto que observa la camara
            uniform float uGiro; // Giro alrededor de Y
            uniform float uAspecto; // Relacion del ancho y alto de la imagen
            uniform int uMapa; // Modo de perspectiva (0, 1)

            out vec3 vMundo; // Posicion mundial al shader de fragmentos
            out vec3 vNormal; // Normal transformada para la iluminacion

            void main() {
                float coseno = cos(uGiro); // Calculo del coseno del giro del objeto
                float seno = sin(uGiro);
                mat3 giro = mat3(
                    coseno, 0.0, -seno,
                    0.0,    1.0, 0.0,
                    seno,   0.0, coseno
                );
                // Escalar, filtrar y trasladar el vertice del mundo
                vMundo = giro * (aPos * uEscala) + uPos;

                // Corregimoss la normal con la inversa transpuesta de la escala y giro
                vNormal = normalize(giro * (aNormal / uEscala));

                if (uMapa == 1) { // Dibujar el minimapa
                    float pantallaX = vMundo.x / 37.0;
                    float pantallaY = vMundo.z / 37.0;
                    float profundidad = -vMundo.y / 100.0;

                    gl_Position = vec4(pantallaX, pantallaY, profundidad, 1.0);
                } else { // Escena principal utilizando la camara como perspectiva
                    vec3 frente = normalize(uObjetivo - uOjo); // Direccion hacia lo que mira la camara
                    vec3 derecha = normalize(cross(frente, vec3(0.0, 1.0, 0.0))); // Eje horizontal de la camara
                    vec3 arriba = cross(derecha, frente); // Eje vertical
                    vec3 diferencia = vMundo - uOjo; // Trasladar el origen del mundo hasta la camara

                    float vistaX = dot(diferencia, derecha);
                    float vistaY = dot(diferencia, arriba);
                    float vistaZ = -dot(diferencia, frente);
                    float factor = 1.0 / tan(radians(55.0) * 0.5);
                    float cerca = 0.1;
                    float lejos = 250.0;
                    float clipX = vistaX * factor / uAspecto;
                    float clipY = vistaY * factor;
                    float clipZ = (lejos + cerca) / (cerca - lejos) * vistaZ;

                    clipZ += 2.0 * lejos * cerca / (cerca - lejos); // Añadir constante de profundidad
                    gl_Position = vec4(clipX, clipY, clipZ, -vistaZ);
                }
            }
        """;
    }

    private String fragmentShader() {
        return """
            #version 330 core
            uniform vec3 uColor;
            out vec4 color;

            void main() {
                color = vec4(uColor, 1.0);
            }
        """;
    }

    // Añadir vehiculos y personas caminando

    public static void main(String[] args) {
        new CityApp().run();
    }
}
