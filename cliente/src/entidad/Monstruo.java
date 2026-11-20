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
    // spritesmonstruo/monstruo.png: 4 cuadros de 16 x 16 en una fila (Ocular Watcher), 200 ms cada uno.
    private static final int TAM_CUADRO = 16;
    private static final int CUADROS = 4;
    private static final long MS_POR_CUADRO = 200;
    private final BufferedImage[] cuadros = new BufferedImage[CUADROS];

    public Monstruo() {
        try (var recurso = getClass().getResourceAsStream("/spritesmonstruo/monstruo.png")) {
            if (recurso == null) throw new IOException("Falta monstruo.png");
            BufferedImage hoja = ImageIO.read(recurso);
            for (int i = 0; i < CUADROS; i++) {
                cuadros[i] = hoja.getSubimage(i * TAM_CUADRO, 0, TAM_CUADRO, TAM_CUADRO);
            }
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo cargar el monstruo", e);
        }
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
        // Todos los monstruos comparten el mismo cuadro de la animacion en cada momento.
        BufferedImage sprite = cuadros[(int) ((System.currentTimeMillis() / MS_POR_CUADRO) % CUADROS)];
        for (Mensaje monstruo : posiciones) {
            int pantallaX = monstruo.x - camaraX;
            int pantallaY = monstruo.y - camaraY;
            g2.drawImage(sprite, pantallaX, pantallaY, size, size, null);
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
