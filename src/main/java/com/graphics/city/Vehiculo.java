package com.graphics.city;

import com.graphics.apps.CityApp;
import static org.lwjgl.glfw.GLFW.*;

public class Vehiculo extends CityApp {


	private float autoX = -30;
    private float autoZ = 30;
    private float angulo = 0;
    private float velocidad = 0;
    private boolean cameraAerea = false;
    private static final float RADIO_AUTO = 1.6f;

    @Override
    protected void tecla(int key) {
        super.tecla(key);

        if (key == GLFW_KEY_C) {
            cameraAerea = !cameraAerea;
        }

        if (key == GLFW_KEY_R) reiniciar();
    }

    private void reiniciar() {
        autoX = -30;
        autoZ = 30;
        angulo = 0;
        velocidad = 0;
    }

    @Override
    protected void actualizar(float deltaTime) {
        float acelerador = 0;

        if (pulsada(GLFW_KEY_W) || pulsada(GLFW_KEY_UP)) {
            acelerador += 1;
        }

        if (pulsada(GLFW_KEY_S) || pulsada(GLFW_KEY_DOWN)) {
            acelerador -= 1;
        }

        velocidad += acelerador * 9 * deltaTime;
        float resistencia = 0.7f;

        if (pulsada(GLFW_KEY_SPACE)) {
            resistencia = 7;
        }

        float factorFrenado = (float) Math.exp(-resistencia * deltaTime);
        velocidad *= factorFrenado;
        velocidad = Math.max(-6, Math.min(16, velocidad));

        float direccion = 0;

        if (pulsada(GLFW_KEY_A) || pulsada(GLFW_KEY_LEFT)) direccion += 1;

        if (pulsada(GLFW_KEY_D) || pulsada(GLFW_KEY_RIGHT)) direccion -= 1;

        angulo += direccion * velocidad * 0.11f * deltaTime;
        float frenteX = -(float) Math.sin(angulo);
        float frenteZ = -(float) Math.cos(angulo);
        float siguienteX = autoX + frenteX * velocidad * deltaTime;
        float siguienteZ = autoZ + frenteZ * velocidad * deltaTime;

        if (puedeCircular(siguienteX, siguienteZ)) {
            autoX = siguienteX;
            autoZ = siguienteZ;
        } else {
            velocidad = 0;
        }
        actualizarTitulo();
    }

    private void actualizarTitulo() {
        int kmPorHora = Math.round(Math.abs(velocidad) * 3.6f);
        String titulo = getClass().getSimpleName();
        titulo += " | WASD/flechas: conducir | Espacio: freno | ";
        titulo += " | C: camara | R: Reiniciar | ";
        titulo += kmPorHora + " km/h";
        titulo += estadoExtra();
        glfwSetWindowTitle(ventana, titulo);
    }

    protected String estadoExtra() {
        return "";
    }

    private boolean puedeCircular(float x, float z) {
        float limitPermitido = LIMITE - RADIO_AUTO;

        if (Math.abs(x) > limitPermitido || Math.abs(z) > limitPermitido) return false;

        for (int fila = 0; fila < map.rows(); fila++) {
            for (int column = 0; column < map.columns(); column++) {
                if (map.valueAt(fila, column) == CityMap.STREET) continue;

                float centroX = centro(column);
                float centroZ = centro(fila);
                float cercaX = Math.max(centroX - 5, Math.min(x, centroX + 5));
                float cercaZ = Math.max(centroZ - 5, Math.min(z, centroZ + 5));

                float distanciaX = x - cercaX;
                float distanciaZ = z - cercaZ;
                float disntanciaCuadrada = distanciaX * distanciaX + distanciaZ * distanciaZ;

                if (disntanciaCuadrada < RADIO_AUTO * RADIO_AUTO) return false;
            }
        }
        return true;
    }

    @Override
    protected void configurarCamara() {
        if (cameraAerea) {
            super.configurarCamara();
            return;
        }

        float camaraX = autoX + (float) Math.sin(angulo) * 12;
        float camaraZ = autoZ + (float) Math.cos(angulo) * 12;

        vector("uOjo", camaraX, 9, camaraZ);
        vector("uObjetivo", autoX, 0.8f, autoZ);
        decimal("uAspecto", (float) ancho/alto);
    }

    // ---------- Dibujo del Auto --------
    @Override
    protected void escena() {
        super.escena();
        dibujarAuto();
    }

    protected void dibujarAuto() {
        pieza(0, 0.65f, 0, 1.65f, 0.55f, 2.6f, 0.95f, 0.24f, 0.12f); // carroceria
        pieza(0, 1.12f, 0.12f, 1.3f, 0.55f, 1.25f, 0.22f, 0.65f, 0.78f); // cabina

        float[] ladosRueda = {-0.88f, 0.88f};
        float[] ejesRueda = {-0.82f, 0.82f};

        for (float x: ladosRueda) {
            for (float z : ejesRueda) {
               pieza(x, 0.38f, z, 0.24f, 0.58f, 0.6f, 0.055f, 0.65f, 0.08f);
            }
        }

        float[] ladosFaros = {-0.55f, 0.55f};
        for (float x : ladosFaros) {
            pieza(x, 0.68f, -1.32f, 0.38f, 0.2f, 0.07f, 1, 0.95f, 0.65f);
            pieza(x, 0.68f, -1.32f, 0.38f, 0.2f, 0.07f, 1, 0.05f, 0.05f);
        }
    }

    private void pieza(float x, float y, float z, float sx, float sy, float sz, float r, float g, float b) {
        float coseno = (float) Math.cos(angulo);
        float seno = (float) Math.sin(angulo);
        float mundoX = autoX + coseno * x + seno * z;
        float mundoZ = autoZ - seno * x + coseno * z;

        cajaGirada(mundoX, y, mundoZ, sx, sy, sz, r, g, b, angulo);
    }

    public static void main(String[] args) {
        new Vehiculo().run();
    }
}
