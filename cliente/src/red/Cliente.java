package red;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;

public class Cliente {
    private final Socket socket;
    private final boolean modoLocal;
    private BufferedReader entrada;
    private BufferedWriter salida;
    private volatile boolean conectado;
    private volatile Mensaje estado = new Mensaje("posiciones");
    private final AtomicReference<Mensaje> correccion = new AtomicReference<>();
    // Ataques recientes para dibujarlos unos instantes (jugador que ataca y objetivo).
    public record Disparo(int jugador, int x, int y, long hasta) {}
    private static final long DURACION_DISPARO_MS = 150;
    private final ConcurrentLinkedQueue<Disparo> disparos = new ConcurrentLinkedQueue<>();
    private volatile String error = "";
    private int id;
    private int x, y;
    private String usuario;

    // La conexion ya fue abierta por ConexionServidor.
    public Cliente(Socket socket) {
        this.socket = socket;
        this.modoLocal = false;
    }

    private Cliente() {
        this.socket = null;
        this.modoLocal = true;
    }

    /** Crea un cliente sin servidor para probar sprites, tiles y sonidos localmente. */
    public static Cliente modoLocal() {
        Cliente local = new Cliente();
        local.id = 0;
        local.x = 100;
        local.y = 100;
        local.usuario = "Local";
        local.conectado = true;

        Mensaje estadoInicial = new Mensaje("posiciones");
        estadoInicial.oleada = 1;
        estadoInicial.restantes = 2;
        estadoInicial.jugadores = new Mensaje[4];
        estadoInicial.jugadores[0] = crearJugadorLocal(0, local.usuario, local.x, local.y);
        estadoInicial.jugadores[1] = crearJugadorLocal(1, "Bot2", 164, 100);
        estadoInicial.jugadores[2] = crearJugadorLocal(2, "Bot3", 228, 100);
        estadoInicial.jugadores[3] = crearJugadorLocal(3, "Bot4", 292, 100);

        Mensaje m1 = new Mensaje();
        m1.x = 288;
        m1.y = 192;
        m1.vida = 30;
        m1.vidaMax = 30;
        Mensaje m2 = new Mensaje();
        m2.x = 384;
        m2.y = 240;
        m2.vida = 20;
        m2.vidaMax = 30;
        estadoInicial.monstruos = new Mensaje[] { m1, m2 };

        local.estado = estadoInicial;
        return local;
    }

    private static Mensaje crearJugadorLocal(int id, String usuario, int x, int y) {
        Mensaje jugador = new Mensaje();
        jugador.id = id;
        jugador.usuario = usuario;
        jugador.x = x;
        jugador.y = y;
        jugador.vida = 100;
        jugador.vidaMax = 100;
        jugador.nivelDanio = 0;
        jugador.nivelVelocidad = 0;
        jugador.velocidad = 4.0;
        return jugador;
    }

    public void autenticar() throws IOException {
        try {
            Properties datos = new Properties();
            String archivo = System.getProperty("config", "cliente.properties");
            try (Reader lector = new FileReader(archivo, StandardCharsets.UTF_8)) {
                datos.load(lector);
            }
            usuario = datos.getProperty("usuario");
            String password = datos.getProperty("password");
            if (usuario == null || usuario.isBlank() || password == null || password.isEmpty()) {
                throw new IOException("Faltan usuario/password en " + archivo);
            }
            socket.setSoTimeout(30000);
            entrada = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            salida = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
            Mensaje login = new Mensaje("autenticar");
            login.usuario = usuario;
            login.password = password;
            enviar(login);
            login.password = null;
            datos.remove("password");
            String linea = entrada.readLine();
            if (linea == null) throw new IOException("El servidor cerro la conexion.");
            Mensaje respuesta = ConversorJSON.desdeJson(linea);
            if (!"autenticar".equals(respuesta.tipo) || !Boolean.TRUE.equals(respuesta.ok)) {
                throw new IOException(respuesta.contenido == null ? "Acceso rechazado" : respuesta.contenido);
            }
            if (respuesta.id == null || respuesta.id < 0 || respuesta.id > 3
                    || respuesta.x == null || respuesta.y == null) {
                throw new IOException("Respuesta de autenticacion incompleta.");
            }
            id = respuesta.id;
            x = respuesta.x;
            y = respuesta.y;
            socket.setSoTimeout(0);
            conectado = true;
            new Thread(() -> recibeMensaje(), "recepcion-json").start();
        } catch (IOException | RuntimeException e) {
            cerrar();
            throw new IOException("No se pudo entrar: " + e.getMessage(), e);
        }
    }

    private void recibeMensaje() {
        try {
            String linea;
            while ((linea = entrada.readLine()) != null) {
                Mensaje mensaje = ConversorJSON.desdeJson(linea);
                if ("correccion".equals(mensaje.tipo) && mensaje.x != null && mensaje.y != null) {
                    correccion.set(mensaje);
                    continue;
                }
                if (!"posiciones".equals(mensaje.tipo) || mensaje.jugadores == null
                        || mensaje.jugadores.length != 4) throw new IOException("Estado invalido");
                estado = mensaje;
                if (mensaje.disparos != null) {
                    long hasta = System.currentTimeMillis() + DURACION_DISPARO_MS;
                    for (Mensaje d : mensaje.disparos) {
                        if (d.id != null && d.x != null && d.y != null) {
                            disparos.add(new Disparo(d.id, d.x, d.y, hasta));
                        }
                    }
                }
            }
            if (conectado) error = "El servidor cerro la conexion.";
        } catch (IOException | RuntimeException e) {
            if (conectado) error = "Se perdio la conexion con el servidor.";
        } finally { cerrar(); }
    }

    public void enviarPosicion(int x, int y) {
        if (modoLocal) {
            this.x = x;
            this.y = y;
            if (estado.jugadores != null && estado.jugadores[0] != null) {
                estado.jugadores[0].x = x;
                estado.jugadores[0].y = y;
            }
            return;
        }
        Mensaje mensaje = new Mensaje("posiciones");
        mensaje.x = x;
        mensaje.y = y;
        try { enviar(mensaje); }
        catch (IOException e) {
            error = "No se pudo enviar la posicion.";
            cerrar();
        }
    }
    private void enviar(Mensaje mensaje) throws IOException {
        salida.write(ConversorJSON.aJson(mensaje));
        salida.newLine();
        salida.flush();
    }
    public void cerrar() {
        conectado = false;
        if (socket != null) try { socket.close(); } catch (IOException ignored) {}
    }
    public boolean esModoLocal() { return modoLocal; }
    public boolean estaConectado() { return conectado; }
    public Mensaje getEstado() { return estado; }
    public Mensaje tomarCorreccion() { return correccion.getAndSet(null); }
    // Ataques que todavia deben verse en pantalla.
    public List<Disparo> getDisparos() {
        long ahora = System.currentTimeMillis();
        disparos.removeIf(d -> d.hasta() < ahora);
        return new ArrayList<>(disparos);
    }
    public String getError() { return error; }
    public int getId() { return id; }
    public int getX() { return x; }
    public int getY() { return y; }
    public String getUsuario() { return usuario; }
}
