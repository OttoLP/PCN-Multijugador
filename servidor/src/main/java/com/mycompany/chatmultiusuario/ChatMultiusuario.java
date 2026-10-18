package com.mycompany.chatmultiusuario;

import java.net.ServerSocket;
import java.sql.Connection;
import java.sql.SQLException;

public class ChatMultiusuario {
    public static void main(String[] args) {
        // Comprueba config.properties y MySQL antes de aceptar jugadores.
        try (Connection prueba = new ConectarMySQL().getConnection()) {
            System.out.println("Conexion a MySQL correcta.");
        } catch (SQLException e) {
            System.err.println("No se pudo conectar a MySQL: " + e.getMessage());
            return;
        }
        try {
            // El puerto del chat original se conserva.
            ServerSocket socket = new ServerSocket(5555);
            System.out.println("Servidor PCN en puerto 5555. Maximo 4 jugadores.");
            new Servidor(socket).iniciaServidor();
        } catch (Exception e) {
            System.err.println("No se pudo iniciar el servidor: " + e.getMessage());
        }
    }
}
