package Main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Scanner;

public class Cliente {

    private Socket socket;
    private BufferedReader recibeMsg;
    private BufferedWriter enviaMsg;

    private String usuario;
    private String contrasena;

    public Cliente(
            Socket socket,
            String usuario,
            String contrasena
    ) throws IOException {

        this.socket = socket;
        this.usuario = usuario;
        this.contrasena = contrasena;

        this.recibeMsg = new BufferedReader(
                new InputStreamReader(socket.getInputStream())
        );

        this.enviaMsg = new BufferedWriter(
                new OutputStreamWriter(socket.getOutputStream())
        );
    }

    public boolean autenticar() {
        try {
        
            enviaMsg.write(usuario);
            enviaMsg.newLine();

            enviaMsg.write(contrasena);
            enviaMsg.newLine();

            enviaMsg.flush();


            String respuesta = recibeMsg.readLine();

            return "AUTH_OK".equals(respuesta);

        } catch (IOException e) {
            System.out.println(
                    "Error durante la autenticación: "
                    + e.getMessage()
            );
            return false;
        }
    }

    public void enviaMensaje() {
        Scanner teclado = new Scanner(System.in);

        try {
            while (socket.isConnected()
                    && !socket.isClosed()) {

                String mensaje = teclado.nextLine();

                enviaMsg.write(
                        usuario + ": " + mensaje
                );

                enviaMsg.newLine();
                enviaMsg.flush();
            }

        } catch (IOException e) {
            cierraTodo();
        }
    }

    public void recibeMensaje() {
        Thread hiloRecepcion = new Thread(() -> {
            try {
                while (socket.isConnected()
                        && !socket.isClosed()) {

                    String mensaje = recibeMsg.readLine();

                    if (mensaje == null) {
                        break;
                    }

                    System.out.println(mensaje);
                }

            } catch (IOException e) {
                System.out.println(
                        "Se perdió la conexión con el servidor."
                );
            } finally {
                cierraTodo();
            }
        });

        hiloRecepcion.start();
    }

    public void cierraTodo() {
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
            e.printStackTrace();
        }
    }
}