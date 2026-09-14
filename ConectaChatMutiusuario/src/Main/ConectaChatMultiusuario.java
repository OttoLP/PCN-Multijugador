package Main;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class ConectaChatMultiusuario {

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);

        try {
            System.out.println("Usuario:");
            String usuario = input.nextLine();

            System.out.println("Contraseña:");
            String contrasena = input.nextLine();

            // dirrecion IP del servidor.
            
            Socket socket = new Socket("IP_DE_LA_VM", 5555);

            Cliente cliente = new Cliente(
                    socket,
                    usuario,
                    contrasena
            );

            if (cliente.autenticar()) {
                System.out.println(
                        "Autenticación correcta. Entraste al chat."
                );

                cliente.recibeMensaje();
                cliente.enviaMensaje();

            } else {
                System.out.println(
                        "Usuario o contraseña incorrectos."
                );

                cliente.cierraTodo();
            }

        } catch (IOException e) {
            System.out.println(
                    "No fue posible conectarse al servidor."
            );
            e.printStackTrace();
        }
    }
}