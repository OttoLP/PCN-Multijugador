package menus;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.ButtonModel;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

public final class EstiloMenu {
    static final Color FONDO = new Color(0,0,0);
    static final Color TEXTO = Color.WHITE;
    static final Color ERROR = new Color(235, 100, 100);
    private static final Dimension TAMANO_BOTON = new Dimension(256, 36);
    private static final String RUTA_FUENTE = "recursos/fuentes/PixelifySans.ttf";
    private static final Font FUENTE_BASE = cargarFuente();
    private static final Color BOTON_NORMAL = new Color(30, 30, 40);
    private static final Color BOTON_HOVER = new Color(60, 60, 90);
    private static final Color BOTON_PRESIONADO = new Color(20, 20, 30);
    private static final Color BOTON_BORDE = new Color(200, 200, 220);

    private EstiloMenu() {}

    public static Font cargarFuente() {
        try {
            Font fuente = Font.createFont(Font.TRUETYPE_FONT, new File(RUTA_FUENTE));
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(fuente);
            return fuente;
        } catch (IOException | FontFormatException e) {
            return new Font("Monospaced", Font.PLAIN, 12);
        }
    }

    public static Font fuente(int estilo, float tamano) {
        return FUENTE_BASE.deriveFont(estilo, tamano);
    }

    public static void aplicarFuenteGlobal() {
        FontUIResource f = new FontUIResource(fuente(Font.PLAIN, 14));
        for (String clave : new String[] {"OptionPane.messageFont", "OptionPane.buttonFont",
                "Button.font", "Label.font"}) {
            UIManager.put(clave, f);
        }
    }

    static JLabel crearTitulo(String texto, int tamano) {
        JLabel titulo = new JLabel(texto, SwingConstants.CENTER);
        titulo.setFont(fuente(Font.BOLD, tamano));
        titulo.setForeground(TEXTO);
        return titulo;
    }

    static JButton crearBoton(String texto) {
        JButton boton = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                ButtonModel m = getModel();
                g2.setColor(m.isPressed() ? BOTON_PRESIONADO : m.isRollover() ? BOTON_HOVER : BOTON_NORMAL);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(BOTON_BORDE);
                g2.setStroke(new BasicStroke(2));
                g2.drawRect(1, 1, getWidth() - 3, getHeight() - 3);
                g2.dispose();
                super.paintComponent(g); // dibuja el texto encima
            }
        };
        boton.setFont(fuente(Font.BOLD, 16));
        boton.setForeground(TEXTO);
        boton.setPreferredSize(TAMANO_BOTON);
        boton.setContentAreaFilled(false);
        boton.setBorderPainted(false);
        boton.setFocusPainted(false);
        return boton;
    }

    static boolean confirmar(Component padre, String titulo, String mensaje, String textoAceptar) {
        Object[] opciones = {textoAceptar, "Cancelar"};
        int eleccion = JOptionPane.showOptionDialog(SwingUtilities.getWindowAncestor(padre), mensaje, titulo,
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, opciones, opciones[1]);
        return eleccion == 0;
    }

    static void agregar(JPanel panel, Component componente, int fila, Insets margen) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = fila;
        c.insets = margen;
        panel.add(componente, c);
    }
}
