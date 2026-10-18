package main;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import red.Cliente;
import red.ConexionServidor;
import java.net.Socket;

public class Game {
    public static void main(String[] args) {
        Cliente cliente;
        try {
            // Primero abre la conexion HOST -> VM.
            Socket socket = new ConexionServidor().conectar();
            cliente = new Cliente(socket);
            // Luego lee usuario/password del archivo y autentica por JSON.
            cliente.autenticar();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "No se pudo entrar", JOptionPane.ERROR_MESSAGE);
            return;
        }
        SwingUtilities.invokeLater(() -> {
            JFrame ventana = new JFrame("PCN - " + cliente.getUsuario());
            GamePanel panel = new GamePanel(cliente);
            ventana.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            ventana.addWindowListener(new WindowAdapter() {
                @Override public void windowClosing(WindowEvent e) { panel.detener(); }
            });
            ventana.setResizable(false);
            ventana.add(panel);
            ventana.pack();
            ventana.setLocationRelativeTo(null);
            ventana.setVisible(true);
            panel.requestFocusInWindow();
            panel.startGameThread();
        });
    }
}
