package tile;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class Tile {
    private BufferedImage image;
    private boolean colision = false;
    private boolean lento = false;   // pantano: se avanza mas despacio
    private boolean cura = false;    // fuente: recupera vida
    private Color colorPromedio = Color.GRAY; // para el minimapa

    // Setters
    public void setImage(BufferedImage image) {
        this.image = image;
        this.colorPromedio = promedio(image);
    }
    public void setColision(boolean valor) { this.colision = valor; }
    public void setLento(boolean valor) { this.lento = valor; }
    public void setCura(boolean valor) { this.cura = valor; }

    // Getters
    public BufferedImage getImage() { return image; }
    public boolean getColision() {
        return colision;
    }
    public boolean getLento() { return lento; }
    public boolean getCura() { return cura; }
    public Color getColorPromedio() { return colorPromedio; }

    private static Color promedio(BufferedImage imagen) {
        long r = 0, g = 0, b = 0;
        int total = imagen.getWidth() * imagen.getHeight();
        for (int y = 0; y < imagen.getHeight(); y++) {
            for (int x = 0; x < imagen.getWidth(); x++) {
                int pixel = imagen.getRGB(x, y);
                r += (pixel >> 16) & 0xFF;
                g += (pixel >> 8) & 0xFF;
                b += pixel & 0xFF;
            }
        }
        return new Color((int) (r / total), (int) (g / total), (int) (b / total));
    }
}
