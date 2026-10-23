package tile;

import java.awt.image.BufferedImage;

public class Tile {
    private BufferedImage image;   
    private boolean colision = false;

    // Setters
    public void setImage(BufferedImage image) {
        this.image = image;
    }
    public void setColision(boolean valor) { this.colision = valor; }

    // Getters
    public BufferedImage getImage() { return image; }
    public boolean getColision() {
        return colision;
    }
}
