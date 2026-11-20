package entidad;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Font;
import java.awt.Graphics2D;
import red.Mensaje;

// Dibuja los objetos del mapa que manda el servidor: barriles y lo que sueltan (corazon, furia, rapidez).
public final class Objetos {
    // Mismo tamano de cuerpo que usa el servidor.
    private static final int CUERPO = 36;
    private static final Color CORAZON = new Color(232, 70, 95);
    private static final Color FURIA = new Color(255, 150, 40);
    private static final Color RAPIDEZ = new Color(80, 200, 255);

    public void draw(Graphics2D g2, Mensaje[] objetos, int camaraX, int camaraY) {
        if (objetos == null) return;
        // Los objetos sueltos suben y bajan un poco para llamar la atencion.
        int balanceo = (int) (Math.sin(System.currentTimeMillis() / 220.0) * 3);
        for (Mensaje objeto : objetos) {
            if (objeto.tipo == null || objeto.x == null || objeto.y == null) continue;
            int cx = objeto.x + CUERPO / 2 - camaraX;
            int cy = objeto.y + CUERPO / 2 - camaraY;
            switch (objeto.tipo) {
                case "barril" -> barril(g2, cx, cy, objeto);
                case "corazon" -> corazon(g2, cx, cy + balanceo);
                case "furia" -> insignia(g2, cx, cy + balanceo, FURIA, "F");
                case "rapidez" -> insignia(g2, cx, cy + balanceo, RAPIDEZ, "R");
                default -> { }
            }
        }
    }

    private void barril(Graphics2D g2, int cx, int cy, Mensaje objeto) {
        int ancho = 26, alto = 30;
        int x = cx - ancho / 2;
        int y = cy - alto / 2;
        g2.setColor(new Color(0, 0, 0, 70));
        g2.fillOval(cx - 15, y + alto - 7, 30, 11);
        g2.setColor(new Color(112, 78, 48));
        g2.fillRoundRect(x, y, ancho, alto, 8, 8);
        g2.setColor(new Color(150, 108, 68));
        g2.fillRoundRect(x + 4, y + 2, 6, alto - 4, 4, 4);
        g2.setColor(new Color(70, 74, 84));
        g2.fillRect(x, y + 7, ancho, 3);
        g2.fillRect(x, y + alto - 10, ancho, 3);
        g2.setColor(new Color(40, 28, 18));
        g2.drawRoundRect(x, y, ancho, alto, 8, 8);
        // La barra de vida solo aparece cuando el barril ya recibio dano.
        if (objeto.vida != null && objeto.vidaMax != null && objeto.vida < objeto.vidaMax) {
            g2.setColor(new Color(20, 20, 20, 200));
            g2.fillRect(x - 1, y - 8, ancho + 2, 5);
            g2.setColor(new Color(190, 40, 40));
            g2.fillRect(x, y - 7, ancho * objeto.vida / Math.max(1, objeto.vidaMax), 3);
        }
    }

    private void corazon(Graphics2D g2, int cx, int cy) {
        brillo(g2, cx, cy, CORAZON);
        g2.setColor(CORAZON);
        g2.fillOval(cx - 12, cy - 11, 13, 13);
        g2.fillOval(cx - 1, cy - 11, 13, 13);
        g2.fillPolygon(new int[] {cx - 12, cx + 12, cx}, new int[] {cy - 3, cy - 3, cy + 12}, 3);
        g2.setColor(new Color(255, 255, 255, 140));
        g2.fillOval(cx - 8, cy - 8, 4, 4);
    }

    // Circulo con una letra: F = furia (mas dano), R = rapidez (mas velocidad).
    private void insignia(Graphics2D g2, int cx, int cy, Color color, String letra) {
        brillo(g2, cx, cy, color);
        g2.setColor(color);
        g2.fillOval(cx - 12, cy - 12, 24, 24);
        g2.setColor(new Color(40, 30, 20));
        g2.drawOval(cx - 12, cy - 12, 24, 24);
        g2.setFont(new Font("SansSerif", Font.BOLD, 15));
        FontMetrics medidas = g2.getFontMetrics();
        g2.setColor(Color.WHITE);
        g2.drawString(letra, cx - medidas.stringWidth(letra) / 2, cy + 5);
    }

    private void brillo(Graphics2D g2, int cx, int cy, Color color) {
        g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 60));
        g2.fillOval(cx - 19, cy - 19, 38, 38);
    }
}
