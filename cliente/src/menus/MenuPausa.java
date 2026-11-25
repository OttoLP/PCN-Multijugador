package menus;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;

// Menu flotante del juego: un velo oscuro sobre toda la pantalla con una caja de opciones al centro.
public class MenuPausa extends JPanel {
    private static final Color VELO = new Color(0, 0, 0, 150);
    private static final Color BORDE = new Color(120, 130, 150);

    private final PanelAjustes ajustes;

    // alReanudar cierra el menu; alCambiarPantalla alterna entre pantalla completa y ventana;
    // alVolverAlMenu termina la partida y regresa al menu principal (tras confirmar).
    public MenuPausa(Runnable alReanudar, Runnable alCambiarPantalla, Runnable alVolverAlMenu) {
        setOpaque(false);
        setLayout(new GridBagLayout());
        // Escucha el raton solo para que los clics no lleguen al juego que esta debajo.
        addMouseListener(new MouseAdapter() {});

        // Sin foco en los controles, el juego conserva el teclado y ESC sigue cerrando el menu.
        ajustes = new PanelAjustes(alCambiarPantalla);
        ajustes.permitirFoco(false);
        JButton botonContinuar = EstiloMenu.crearBoton("Continuar");
        botonContinuar.setFocusable(false);
        botonContinuar.addActionListener(e -> alReanudar.run());
        JButton botonRegresar = EstiloMenu.crearBoton("Regresar al menú");
        botonRegresar.setFocusable(false);
        botonRegresar.addActionListener(e -> confirmarRegreso(alVolverAlMenu));

        JPanel caja = new JPanel(new GridBagLayout());
        caja.setBackground(EstiloMenu.FONDO);
        caja.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE), BorderFactory.createEmptyBorder(28, 48, 28, 48)));
        EstiloMenu.agregar(caja, EstiloMenu.crearTitulo("Menú", 40), 0, new Insets(0, 0, 28, 0));
        EstiloMenu.agregar(caja, ajustes, 1, new Insets(0, 0, 28, 0));
        EstiloMenu.agregar(caja, botonContinuar, 2, new Insets(0, 0, 12, 0));
        EstiloMenu.agregar(caja, botonRegresar, 3, new Insets(0, 0, 0, 0));

        add(caja);
        setVisible(false);
    }

    private void confirmarRegreso(Runnable alVolverAlMenu) {
        if (EstiloMenu.confirmar(this, "Regresar al menú",
                "¿Regresar al menú principal? Saldrás de la partida.", "Regresar")) {
            alVolverAlMenu.run();
        } else if (getParent() != null) {
            getParent().requestFocusInWindow(); // el juego recupera el teclado
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(VELO);
        g.fillRect(0, 0, getWidth(), getHeight());
    }

    public void actualizarModoPantalla(boolean pantallaCompleta) {
        ajustes.actualizarModoPantalla(pantallaCompleta);
    }
}
