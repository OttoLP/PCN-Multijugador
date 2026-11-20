package main;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import entidad.Jugador;
import red.Mensaje;
import tile.ManejadorTiles;

// Minimapa del mundo completo (esquina inferior derecha): terreno, jugadores, monstruos y objetos.
public final class Minimapa {
    private static final int PX_POR_TILE = 3;
    private final BufferedImage base;
    private final int ancho;
    private final int alto;

    public Minimapa(ManejadorTiles tiles, int filas, int columnas) {
        ancho = columnas * PX_POR_TILE;
        alto = filas * PX_POR_TILE;
        base = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);
        for (int fila = 0; fila < filas; fila++) {
            for (int col = 0; col < columnas; col++) {
                Color color = aclarar(tiles.getTile(tiles.getCodigoMapaTiles(fila, col)).getColorPromedio());
                for (int dy = 0; dy < PX_POR_TILE; dy++) {
                    for (int dx = 0; dx < PX_POR_TILE; dx++) {
                        base.setRGB(col * PX_POR_TILE + dx, fila * PX_POR_TILE + dy, color.getRGB());
                    }
                }
            }
        }
    }

    // Los colores del terreno son oscuros; en el minimapa se aclaran para que se lea.
    private static Color aclarar(Color c) {
        return new Color(Math.min(255, c.getRed() * 3 / 2 + 10), Math.min(255, c.getGreen() * 3 / 2 + 10),
                Math.min(255, c.getBlue() * 3 / 2 + 10));
    }

    public int getAncho() { return ancho; }
    public int getAlto() { return alto; }

    public void draw(Graphics2D g2, int x, int y, Jugador[] jugadores, int local, Mensaje[] monstruos,
                     Mensaje[] objetos, int camaraX, int camaraY, int pantallaAncho, int pantallaAlto, int tileSize) {
        double escala = (double) PX_POR_TILE / tileSize;
        g2.setColor(new Color(10, 10, 10, 210));
        g2.fillRect(x - 3, y - 3, ancho + 6, alto + 6);
        g2.drawImage(base, x, y, null);

        if (objetos != null) {
            for (Mensaje objeto : objetos) {
                if (objeto.tipo == null || objeto.x == null || objeto.y == null) continue;
                switch (objeto.tipo) {
                    case "barril" -> g2.setColor(new Color(190, 140, 80));
                    case "corazon" -> g2.setColor(new Color(255, 90, 120));
                    case "furia" -> g2.setColor(new Color(255, 160, 40));
                    default -> g2.setColor(new Color(90, 210, 255));
                }
                int tam = objeto.tipo.equals("barril") ? 3 : 5;
                g2.fillRect(x + (int) (objeto.x * escala) - tam / 2 + 1, y + (int) (objeto.y * escala) - tam / 2 + 1, tam, tam);
            }
        }
        if (monstruos != null) {
            g2.setColor(new Color(215, 90, 235));
            for (Mensaje monstruo : monstruos) {
                g2.fillRect(x + (int) (monstruo.x * escala), y + (int) (monstruo.y * escala), 2, 2);
            }
        }
        // Zona que se ve en pantalla
        g2.setColor(new Color(255, 255, 255, 150));
        g2.setStroke(new BasicStroke(1));
        g2.drawRect(x + (int) (camaraX * escala), y + (int) (camaraY * escala),
                (int) (pantallaAncho * escala), (int) (pantallaAlto * escala));
        // Jugadores: caidos en gris; el local con borde blanco
        for (int i = 0; i < jugadores.length; i++) {
            if (jugadores[i] == null) continue;
            int px = x + (int) (jugadores[i].getX() * escala) - 2;
            int py = y + (int) (jugadores[i].getY() * escala) - 2;
            if (i == local) {
                g2.setColor(Color.WHITE);
                g2.fillRect(px - 1, py - 1, 7, 7);
            }
            g2.setColor(jugadores[i].isMuerto() ? Color.GRAY : Jugador.COLORES[i]);
            g2.fillRect(px, py, 5, 5);
        }
    }
}
