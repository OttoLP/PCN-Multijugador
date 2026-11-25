package menus;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class PantallaCreditos extends FondoMenu {
    private static final String RUTA_CREDITOS = "recursos/creditos.txt";
    private static final Color FONDO_TEXTO = new Color(20, 22, 28);
    private static final Color BORDE_TEXTO = new Color(120, 130, 150);

    public PantallaCreditos(Runnable alVolver) {
        setBackground(EstiloMenu.FONDO);
        setLayout(new GridBagLayout());
        construirInterfaz(alVolver);
    }

    private void construirInterfaz(Runnable alVolver) {
        JTextArea texto = new JTextArea(leerCreditos());
        texto.setEditable(false);
        texto.setLineWrap(true);
        texto.setWrapStyleWord(true);
        texto.setFont(new Font("Monospaced", Font.PLAIN, 16));
        texto.setForeground(EstiloMenu.TEXTO);
        texto.setBackground(FONDO_TEXTO);
        texto.setMargin(new Insets(16, 20, 16, 20));
        texto.setCaretPosition(0);

        JScrollPane desplazable = new JScrollPane(texto);
        desplazable.setPreferredSize(new Dimension(720, 480));
        desplazable.setBorder(BorderFactory.createLineBorder(BORDE_TEXTO));

        JButton botonVolver = EstiloMenu.crearBoton("Volver");
        botonVolver.addActionListener(e -> alVolver.run());

        EstiloMenu.agregar(this, EstiloMenu.crearTitulo("Créditos", 56), 0, new Insets(0, 0, 30, 0));
        EstiloMenu.agregar(this, desplazable, 1, new Insets(0, 0, 30, 0));
        EstiloMenu.agregar(this, botonVolver, 2, new Insets(0, 0, 0, 0));
    }

    private String leerCreditos() {
        try {
            return Files.readString(Path.of(RUTA_CREDITOS), StandardCharsets.UTF_8);
        } catch (NoSuchFileException e) {
            return "No se encontró el archivo " + RUTA_CREDITOS + ".";
        } catch (IOException e) {
            return "No se pudieron leer los créditos.";
        }
    }
}
