package menus;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;

import main.Sonido;

// Controles de volumen de la musica y modo de pantalla, compartidos por la pantalla de configuracion
// y por el menu de pausa del juego.
public class PanelAjustes extends JPanel {
    private final JLabel etiquetaVolumen = new JLabel("", SwingConstants.CENTER);
    private final JSlider barraVolumen = new JSlider(0, 100, Sonido.getVolumenMusica());
    private final JButton botonPantalla = EstiloMenu.crearBoton("");

    // alCambiarPantalla alterna entre pantalla completa y ventana.
    public PanelAjustes(Runnable alCambiarPantalla) {
        setOpaque(false);
        setLayout(new GridBagLayout());

        etiquetaVolumen.setFont(EstiloMenu.fuente(Font.BOLD, 18));
        etiquetaVolumen.setForeground(EstiloMenu.TEXTO);
        mostrarVolumen();

        barraVolumen.setOpaque(false);
        barraVolumen.setPreferredSize(new Dimension(320, 30));
        barraVolumen.addChangeListener(e -> cambiarVolumen());

        botonPantalla.addActionListener(e -> alCambiarPantalla.run());

        EstiloMenu.agregar(this, etiquetaVolumen, 0, new Insets(0, 0, 6, 0));
        EstiloMenu.agregar(this, barraVolumen, 1, new Insets(0, 0, 24, 0));
        EstiloMenu.agregar(this, botonPantalla, 2, new Insets(0, 0, 0, 0));
    }

    private void cambiarVolumen() {
        Sonido.setVolumenMusica(barraVolumen.getValue());
        mostrarVolumen();
    }

    private void mostrarVolumen() {
        etiquetaVolumen.setText("Volumen de la música: " + barraVolumen.getValue() + " %");
    }

    // Vuelve a leer el volumen actual (pudo cambiar desde el menu de pausa).
    public void sincronizarVolumen() {
        barraVolumen.setValue(Sonido.getVolumenMusica());
    }

    public void actualizarModoPantalla(boolean pantallaCompleta) {
        botonPantalla.setText("Pantalla: " + (pantallaCompleta ? "completa" : "ventana"));
    }

    // Un componente con foco le quita las teclas al juego; el menu de pausa los deja sin foco.
    public void permitirFoco(boolean permitir) {
        barraVolumen.setFocusable(permitir);
        botonPantalla.setFocusable(permitir);
    }
}
