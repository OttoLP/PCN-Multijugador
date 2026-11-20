package entidad;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import red.Mensaje;

public final class Monstruo {
    // Mismo tamano de colision que usa el servidor.
    private static final int CUERPO = 36;
    private static final int FRAME_SIZE = 16;
    private final BufferedImage[] caminarAbajo;
    private final BufferedImage[] caminarArriba;
    private final BufferedImage[] caminarIzquierda;
    private final BufferedImage[] caminarDerecha;
    private final BufferedImage[] idleAbajo;
    private final BufferedImage[] idleArriba;
    private final BufferedImage[] idleIzquierda;
    private final BufferedImage[] idleDerecha;
    private int[] prevX = new int[0];
    private int[] prevY = new int[0];
    private long[] ultimoMovimiento = new long[0];
    private String[] direcciones = new String[0];

    public Monstruo() {
        try {
            caminarAbajo = cargarTira("/spritesmonstruo/esqueleto_caminar_abajo.png");
            caminarArriba = cargarTira("/spritesmonstruo/esqueleto_caminar_arriba.png");
            caminarIzquierda = cargarTira("/spritesmonstruo/esqueleto_caminar_izq.png");
            caminarDerecha = voltearTira(caminarIzquierda);
            idleAbajo = cargarTira("/spritesmonstruo/esqueleto_idle_abajo.png");
            idleArriba = cargarTira("/spritesmonstruo/esqueleto_idle_arriba.png");
            idleIzquierda = cargarTira("/spritesmonstruo/esqueleto_idle_izq.png");
            idleDerecha = voltearTira(idleIzquierda);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo cargar el monstruo", e);
        }
    }

    private static BufferedImage[] cargarTira(String ruta) throws IOException {
        try (var recurso = Monstruo.class.getResourceAsStream(ruta)) {
            if (recurso == null) throw new IOException("Falta " + ruta);
            BufferedImage tira = ImageIO.read(recurso);
            int columnas = Math.max(1, tira.getWidth() / FRAME_SIZE);
            BufferedImage[] frames = new BufferedImage[columnas];
            for (int i = 0; i < columnas; i++) {
                frames[i] = tira.getSubimage(i * FRAME_SIZE, 0, FRAME_SIZE, FRAME_SIZE);
            }
            return frames;
        }
    }

    private static BufferedImage[] voltearTira(BufferedImage[] origen) {
        BufferedImage[] volteados = new BufferedImage[origen.length];
        for (int i = 0; i < origen.length; i++) {
            BufferedImage src = origen[i];
            BufferedImage dst = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = dst.createGraphics();
            g.drawImage(src, src.getWidth(), 0, -src.getWidth(), src.getHeight(), null);
            g.dispose();
            volteados[i] = dst;
        }
        return volteados;
    }

    public boolean choca(int x, int y, Mensaje[] posiciones) {
        if (posiciones == null) return false;
        var jugador = new Rectangle2D.Double(x, y, CUERPO, CUERPO);
        for (Mensaje monstruo : posiciones) {
            if (jugador.intersects(monstruo.x, monstruo.y, CUERPO, CUERPO)) return true;
        }
        return false;
    }

    public void draw(Graphics2D g2, Mensaje[] posiciones, int camaraX, int camaraY, int size) {
        if (posiciones == null) return;
        if (prevX.length != posiciones.length) {
            prevX = new int[posiciones.length];
            prevY = new int[posiciones.length];
            ultimoMovimiento = new long[posiciones.length];
            direcciones = new String[posiciones.length];
            for (int i = 0; i < posiciones.length; i++) {
                prevX[i] = posiciones[i].x;
                prevY[i] = posiciones[i].y;
                direcciones[i] = "abajo";
            }
        }
        long ahora = System.currentTimeMillis();
        int frame = (int) ((ahora / 140) % caminarAbajo.length);
        for (int i = 0; i < posiciones.length; i++) {
            Mensaje monstruo = posiciones[i];
            int dx = monstruo.x - prevX[i];
            int dy = monstruo.y - prevY[i];
            if (dx != 0 || dy != 0) {
                ultimoMovimiento[i] = ahora;
                if (Math.abs(dx) > Math.abs(dy)) {
                    direcciones[i] = dx > 0 ? "derecha" : "izquierda";
                } else {
                    direcciones[i] = dy > 0 ? "abajo" : "arriba";
                }
            }
            prevX[i] = monstruo.x;
            prevY[i] = monstruo.y;

            boolean moviendo = (ahora - ultimoMovimiento[i]) < 150;
            BufferedImage[] anim;
            if (moviendo) {
                anim = switch (direcciones[i]) {
                    case "arriba" -> caminarArriba;
                    case "izquierda" -> caminarIzquierda;
                    case "derecha" -> caminarDerecha;
                    default -> caminarAbajo;
                };
            } else {
                anim = switch (direcciones[i]) {
                    case "arriba" -> idleArriba;
                    case "izquierda" -> idleIzquierda;
                    case "derecha" -> idleDerecha;
                    default -> idleAbajo;
                };
            }
            int pantallaX = monstruo.x - camaraX;
            int pantallaY = monstruo.y - camaraY;
            g2.drawImage(anim[frame % anim.length], pantallaX, pantallaY, size, size, null);
            // La barra solo aparece cuando el monstruo ya recibio dano.
            if (monstruo.vida != null && monstruo.vidaMax != null && monstruo.vida < monstruo.vidaMax) {
                int ancho = size - 12;
                g2.setColor(new Color(20, 20, 20, 200));
                g2.fillRect(pantallaX + 5, pantallaY - 6, ancho + 2, 5);
                g2.setColor(new Color(190, 40, 40));
                g2.fillRect(pantallaX + 6, pantallaY - 5, ancho * monstruo.vida / Math.max(1, monstruo.vidaMax), 3);
            }
        }
    }
}
