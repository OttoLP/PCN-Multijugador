package menus;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

class FondoMenu extends JPanel {
    private static final Image IMAGEN = cargar();

    private static Image cargar() {
        try { return ImageIO.read(new File("recursos/fondo.jpg")); }
        catch (IOException e) { return null; }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (IMAGEN == null) return;
        int w = IMAGEN.getWidth(null), h = IMAGEN.getHeight(null);
        double esc = Math.max((double) getWidth() / w, (double) getHeight() / h);
        int aw = (int) (w * esc), ah = (int) (h * esc);
        Graphics2D g2 = (Graphics2D) g.create();
        // Vecino mas cercano para que el pixel art no se vea borroso.
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2.drawImage(IMAGEN, (getWidth() - aw) / 2, (getHeight() - ah) / 2, aw, ah, null);
        g2.dispose();
    }
}
