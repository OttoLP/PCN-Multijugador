
package com.mycompany.chatmultiusuario;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConectarMySQL {

    // Los datos de MySQL ya no estan escritos aqui: se leen de config.properties
    // (carpeta servidor/). Cada integrante usa el suyo y no se sube a git.
    // Otra ruta: -Dconfig=ruta/al/archivo.properties
    private static Properties config;

    private static synchronized Properties config() throws SQLException {
        if (config == null) {
            String archivo = System.getProperty("config", "config.properties");
            Properties datos = new Properties();
            try (Reader lector = Files.newBufferedReader(
                    Path.of(archivo), StandardCharsets.UTF_8)) {
                datos.load(lector);
            } catch (IOException e) {
                throw new SQLException("No se pudo leer " + archivo
                        + ". Copia config.example.properties como config.properties"
                        + " y escribe tus datos de MySQL.", e);
            }
            String usuario = datos.getProperty("db.user");
            if (usuario == null || usuario.isBlank()
                    || usuario.startsWith("TU_")) {
                throw new SQLException("Escribe tu usuario de MySQL en "
                        + archivo + " (db.user).");
            }
            config = datos;
        }
        return config;
    }

    public Connection getConnection() throws SQLException {
        Properties datos = config();
        String url = "jdbc:mysql://"
                + datos.getProperty("db.host", "localhost").trim()
                + ":" + datos.getProperty("db.port", "3306").trim()
                + "/" + datos.getProperty("db.name", "restordb").trim()
                + "?useSSL=false"
                + "&allowPublicKeyRetrieval=true"
                + "&serverTimezone=UTC";
        return DriverManager.getConnection(
                url,
                datos.getProperty("db.user").trim(),
                datos.getProperty("db.password", "")
        );
    }
}
