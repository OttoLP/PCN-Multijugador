package com.mycompany.chatmultiusuario;

import java.io.IOException;
import java.net.ServerSocket;

public class ChatMultiusuario {

    public static void main(String[] args) {
        try {
            ServerSocket socketServidor =
                    new ServerSocket(5555);

            Servidor servidor = new Servidor(socketServidor);

            System.out.println(
                    "Servidor iniciado en el puerto 5555..."
            );

            servidor.iniciaServidor();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}