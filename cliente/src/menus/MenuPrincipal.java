package menus;

import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.net.Socket;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import red.Cliente;
import red.ConexionServidor;

// Pantalla inicial.
public class MenuPrincipal extends FondoMenu {
    private static final int CANTIDAD_JUGADORES = 4;
    private static final String RUTA_JUGADOR = "config/Jugador%d.properties";

    private final Consumer<Cliente> alConectar;
    private final Runnable alAbrirConfiguracion;
    private final Runnable alAbrirCreditos;
    private final Runnable alSalir;
    private final JButton botonJugar = EstiloMenu.crearBoton("Jugar");
    private final JButton botonConfiguracion = EstiloMenu.crearBoton("Configuración");
    private final JButton botonCreditos = EstiloMenu.crearBoton("Créditos");
    private final JButton botonSalir = EstiloMenu.crearBoton("Salir");
    private final JLabel etiquetaEstado = new JLabel(" ", SwingConstants.CENTER);
    private volatile boolean cancelado;

    public MenuPrincipal(Consumer<Cliente> alConectar, Runnable alAbrirConfiguracion,
                         Runnable alAbrirCreditos, Runnable alSalir) {
        this.alConectar = alConectar;
        this.alAbrirConfiguracion = alAbrirConfiguracion;
        this.alAbrirCreditos = alAbrirCreditos;
        this.alSalir = alSalir;
        setBackground(EstiloMenu.FONDO);
        setLayout(new GridBagLayout());
        construirInterfaz();
    }

    private void construirInterfaz() {
        botonJugar.addActionListener(e -> alPulsarJugar());
        botonConfiguracion.addActionListener(e -> alAbrirConfiguracion.run());
        botonCreditos.addActionListener(e -> alAbrirCreditos.run());
        botonSalir.addActionListener(e -> confirmarSalida());

        etiquetaEstado.setFont(EstiloMenu.fuente(Font.PLAIN, 24));
        etiquetaEstado.setForeground(EstiloMenu.TEXTO);

        EstiloMenu.agregar(this, EstiloMenu.crearTitulo("PCN", 72), 0, new Insets(0, 0, 40, 0));
        EstiloMenu.agregar(this, botonJugar, 1, new Insets(0, 0, 12, 0));
        EstiloMenu.agregar(this, botonConfiguracion, 2, new Insets(0, 0, 12, 0));
        EstiloMenu.agregar(this, botonCreditos, 3, new Insets(0, 0, 12, 0));
        EstiloMenu.agregar(this, botonSalir, 4, new Insets(0, 0, 24, 0));
        EstiloMenu.agregar(this, etiquetaEstado, 5, new Insets(0, 0, 0, 0));
    }

    private void confirmarSalida() {
        if (EstiloMenu.confirmar(this, "Salir", "¿Seguro que quieres salir del juego?", "Salir")) {
            alSalir.run();
        }
    }

    private void alPulsarJugar() {
        iniciarConexion();
    }

    // Prueba los jugadores 1 a 4 en orden y se queda con el primero que logre autenticarse.
    // La conexion y la autenticacion pueden tardar segundos: se hacen fuera del hilo de Swing.
    private void iniciarConexion() {
        cancelado = false;
        botonJugar.setEnabled(false);
        mostrarEstado("Conectando con el servidor...", false);
        new SwingWorker<Cliente, Void>() {
            @Override
            protected Cliente doInBackground() throws Exception {
                for (int i = 1; i <= CANTIDAD_JUGADORES && !cancelado; i++) {
                    // Si no se puede abrir la conexion, el servidor esta caido: el error sale tal cual.
                    Socket socket = new ConexionServidor().conectar();
                    Cliente cliente = new Cliente(socket);
                    try {
                        cliente.autenticar(String.format(RUTA_JUGADOR, i));
                        return cliente;
                    } catch (Exception e) {
                        // Jugador ocupado (o rechazado): se cierra y se intenta con el siguiente.
                        cliente.cerrar();
                    }
                }
                if (cancelado) return null;
                throw new Exception("El servidor está lleno.");
            }

            @Override
            protected void done() {
                try {
                    Cliente cliente = get();
                    if (cliente == null) return;
                    if (cancelado) {
                        cliente.cerrar();
                        return;
                    }
                    alConectar.accept(cliente);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    if (cancelado) return;
                    mostrarEstado(mensajeDeError(e.getCause()), true);
                    botonJugar.setEnabled(true);
                }
            }
        }.execute();
    }

    public void restablecer() {
        botonJugar.setEnabled(true);
        mostrarEstado(" ", false);
    }

    public void cancelar() {
        cancelado = true;
    }

    private String mensajeDeError(Throwable causa) {
        if (causa == null || causa.getMessage() == null || causa.getMessage().isBlank()) {
            return "No se pudo conectar con el servidor.";
        }
        return causa.getMessage();
    }

    private void mostrarEstado(String texto, boolean error) {
        etiquetaEstado.setForeground(error ? EstiloMenu.ERROR : EstiloMenu.TEXTO);
        etiquetaEstado.setText(texto);
    }
}
