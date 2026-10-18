package com.mycompany.chatmultiusuario;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Servidor {
    private final ServerSocket socketServidor;

    public Servidor(ServerSocket socketServidor) {
        this.socketServidor = socketServidor;
    }
    public void iniciaServidor() throws IOException {
        while (!socketServidor.isClosed()) {
            Socket socket = socketServidor.accept();
            new Thread(new ManejadorClientes(socket)).start();
        }
    }
}
