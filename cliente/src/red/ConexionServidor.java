package red;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class ConexionServidor {

    private static final int PUERTO = 5555;

    // La IP ya no esta escrita en el codigo: se lee de servidor.properties
    // (carpeta cliente/). Cada integrante usa la suya y no se sube a git.
    // Otra ruta: -Dservidor.config=ruta/al/archivo.properties
    private String leerIp() throws IOException {
        String archivo = System.getProperty("servidor.config", "servidor.properties");
        Properties datos = new Properties();
        try (Reader lector = new FileReader(archivo, StandardCharsets.UTF_8)) {
            datos.load(lector);
        } catch (FileNotFoundException e) {
            throw new IOException("No existe " + archivo + ". Copia servidor.properties.example "
                    + "como servidor.properties y escribe la IP de tu VM.");
        }
        String ip = datos.getProperty("servidor.ip");
        if (ip == null || ip.isBlank() || "IP_DE_LA_VM".equals(ip.trim())) {
            throw new IOException("Escribe la IP real de tu VM en " + archivo + " (servidor.ip).");
        }
        return ip.trim();
    }

    public Socket conectar() throws IOException {
        String ip = leerIp();
        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(ip, PUERTO), 5000);
            return socket;
        } catch (IOException e) {
            socket.close();
            throw e;
        }
    }
}
