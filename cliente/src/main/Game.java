package main;

import java.awt.CardLayout;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import menus.MenuPrincipal;
import menus.PantallaConfiguracion;
import menus.PantallaCreditos;
import menus.EstiloMenu;

import red.Cliente;

public class Game {
    private static final String TARJETA_MENU = "menu";
    private static final String TARJETA_CONFIGURACION = "configuracion";
    private static final String TARJETA_CREDITOS = "creditos";
    private static final String TARJETA_JUEGO = "juego";
    private static final String ACCION_PANTALLA = "alternarPantalla";

    private final JFrame ventana = new JFrame("PCN");
    private final GraphicsDevice pantalla =
            GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
    private final CardLayout tarjetas = new CardLayout();
    private final JPanel contenedor = new JPanel(tarjetas);
    private final MenuPrincipal menu =
            new MenuPrincipal(this::iniciarJuego, this::mostrarConfiguracion, this::mostrarCreditos,
                    this::cerrarAplicacion);
    private final PantallaConfiguracion configuracion =
            new PantallaConfiguracion(this::mostrarMenu, this::alternarPantalla);
    private final PantallaCreditos creditos = new PantallaCreditos(this::mostrarMenu);
    private GamePanel panelJuego;
    private boolean pantallaCompleta = true;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            EstiloMenu.aplicarFuenteGlobal();
            new Game().iniciar();
        });
    }

    private void iniciar() {
        contenedor.setPreferredSize(GamePanel.TAMANO_PANTALLA);
        contenedor.add(menu, TARJETA_MENU);
        contenedor.add(configuracion, TARJETA_CONFIGURACION);
        contenedor.add(creditos, TARJETA_CREDITOS);

        ventana.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        ventana.addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { cerrarAplicacion(); }
        });
        ventana.add(contenedor);
        registrarTeclaPantalla();
        aplicarPantalla();
    }

    private void mostrarMenu() {
        tarjetas.show(contenedor, TARJETA_MENU);
    }

    private void mostrarConfiguracion() {
        configuracion.sincronizarVolumen();
        tarjetas.show(contenedor, TARJETA_CONFIGURACION);
    }

    private void mostrarCreditos() {
        tarjetas.show(contenedor, TARJETA_CREDITOS);
    }

    // F11 para cambiar entre ventana o pantalla completa
    private void registrarTeclaPantalla() {
        JRootPane raiz = ventana.getRootPane();
        raiz.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0), ACCION_PANTALLA);
        raiz.getActionMap().put(ACCION_PANTALLA, new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { alternarPantalla(); }
        });
    }

    private void alternarPantalla() {
        pantallaCompleta = !pantallaCompleta;
        aplicarPantalla();
    }

    private void aplicarPantalla() {
        ventana.dispose();
        ventana.setUndecorated(pantallaCompleta);
        ventana.setResizable(pantallaCompleta);
        ventana.setAlwaysOnTop(pantallaCompleta);
        if (pantallaCompleta) {
            ventana.setBounds(pantalla.getDefaultConfiguration().getBounds());
        } else {
            ventana.pack();
            ventana.setLocationRelativeTo(null);
        }
        ventana.setVisible(true);
        configuracion.actualizarModoPantalla(pantallaCompleta);
        if (panelJuego != null) panelJuego.actualizarModoPantalla(pantallaCompleta);
        devolverFoco();
    }

    private void iniciarJuego(Cliente cliente) {
        panelJuego = new GamePanel(cliente, this::alternarPantalla, this::volverAlMenuPrincipal);
        panelJuego.actualizarModoPantalla(pantallaCompleta);
        contenedor.add(panelJuego, TARJETA_JUEGO);
        tarjetas.show(contenedor, TARJETA_JUEGO);
        ventana.setTitle("PCN - " + cliente.getUsuario());
        devolverFoco();
        panelJuego.startGameThread();
    }

    private void volverAlMenuPrincipal() {
        panelJuego.detener();
        contenedor.remove(panelJuego);
        panelJuego = null;
        ventana.setTitle("PCN");
        menu.restablecer();
        mostrarMenu();
    }

    private void devolverFoco() {
        if (panelJuego != null) SwingUtilities.invokeLater(panelJuego::requestFocusInWindow);
    }

    private void cerrarAplicacion() {
        menu.cancelar();
        if (panelJuego != null) panelJuego.detener();
        ventana.dispose();
    }
}
