
package com.mycompany.chatmultiusuario;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;

public class ManejadorClientes implements Runnable {

    public static ArrayList<ManejadorClientes> arregloClientes =
            new ArrayList<>();

    private Socket socket;
    private BufferedReader recibeMsg;
    private BufferedWriter enviaMsg;

    private String usuario;
    private boolean autenticado;

    public ManejadorClientes(Socket socket) {
        try {
            this.socket = socket;

            this.enviaMsg = new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream())
            );

            this.recibeMsg = new BufferedReader(
                    new InputStreamReader(socket.getInputStream())
            );

            //Verificacion de las credenciales
            this.usuario = recibeMsg.readLine();
            String contrasena = recibeMsg.readLine();

            UsuarioDAO usuarioDAO = new UsuarioDAO();
            autenticado = usuarioDAO.autenticar(usuario, contrasena);

            if (autenticado) {
                enviaMsg.write("AUTH_OK");
                enviaMsg.newLine();
                enviaMsg.flush();

                synchronized (arregloClientes) {
                    arregloClientes.add(this);
                }

                System.out.println(
                        "Usuario autenticado: " + usuario
                );

                broadcastMensaje(
                        "SERVIDOR: " + usuario + " ha entrado al chat!"
                );

            } else {
                enviaMsg.write("AUTH_ERROR");
                enviaMsg.newLine();
                enviaMsg.flush();

                System.out.println(
                        "Intento de acceso rechazado para: " + usuario
                );
            }

        } catch (IOException e) {
            autenticado = false;
            System.out.println(
                    "Error al recibir las credenciales: " + e.getMessage()
            );
        }
    }

    public boolean estaAutenticado() {
        return autenticado;
    }

    @Override
    public void run() {
        if (!autenticado) {
            cierraConexion();
            return;
        }

        while (socket != null && !socket.isClosed()) {
            try {
                String mensajeCliente = recibeMsg.readLine();

                if (mensajeCliente == null) {
                    break;
                }

                broadcastMensaje(mensajeCliente);

            } catch (IOException e) {
                break;
            }
        }

        cierraTodo();
    }

    public void broadcastMensaje(String mensaje) {
        synchronized (arregloClientes) {
            for (ManejadorClientes cliente : arregloClientes) {
                try {
                    if (cliente != this) {
                        cliente.enviaMsg.write(mensaje);
                        cliente.enviaMsg.newLine();
                        cliente.enviaMsg.flush();
                    }

                } catch (IOException e) {
                    System.out.println(
                            "No se pudo enviar un mensaje a "
                            + cliente.usuario
                    );
                }
            }
        }
    }

    public void eliminaManejadorCliente() {
        boolean eliminado;

        synchronized (arregloClientes) {
            eliminado = arregloClientes.remove(this);
        }

        if (eliminado) {
            broadcastMensaje(
                    "SERVIDOR: " + usuario + " ha abandonado el chat!"
            );

            System.out.println(
                    "El usuario " + usuario + " se desconectó."
            );
        }
    }

    public void cierraTodo() {
        eliminaManejadorCliente();
        cierraConexion();
    }

    private void cierraConexion() {
        try {
            if (recibeMsg != null) {
                recibeMsg.close();
            }

            if (enviaMsg != null) {
                enviaMsg.close();
            }

            if (socket != null && !socket.isClosed()) {
                socket.close();
            }

        } catch (IOException e) {
            System.out.println(
                    "Error al cerrar la conexión: " + e.getMessage()
            );
        }
    }
}