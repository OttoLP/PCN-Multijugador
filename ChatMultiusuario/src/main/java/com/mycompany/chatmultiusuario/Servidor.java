package com.mycompany.chatmultiusuario;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Servidor {

    private ServerSocket socketServidor;

    public Servidor(ServerSocket socketServidor) {
        this.socketServidor = socketServidor;
    }

    public void iniciaServidor() {
        try {
            while (!socketServidor.isClosed()) {
                Socket socket = socketServidor.accept();

                System.out.println(
                        "Un cliente intenta conectarse..."
                );

                ManejadorClientes manejador =
                        new ManejadorClientes(socket);

                if (manejador.estaAutenticado()) {
                    Thread hilo = new Thread(manejador);
                    hilo.start();
                }
            }

        } catch (IOException e) {
            cierraSocketServidor();
        }
    }

    public void cierraSocketServidor() {
        try {
            if (socketServidor != null
                    && !socketServidor.isClosed()) {
                socketServidor.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}