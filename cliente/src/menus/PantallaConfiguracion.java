package menus;

import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JButton;

// Pantalla de configuracion
public class PantallaConfiguracion extends FondoMenu {
    private final PanelAjustes ajustes;

    public PantallaConfiguracion(Runnable alVolver, Runnable alCambiarPantalla) {
        setBackground(EstiloMenu.FONDO);
        setLayout(new GridBagLayout());
        ajustes = new PanelAjustes(alCambiarPantalla);

        JButton botonVolver = EstiloMenu.crearBoton("Volver");
        botonVolver.addActionListener(e -> alVolver.run());

        EstiloMenu.agregar(this, EstiloMenu.crearTitulo("Configuración", 56), 0, new Insets(0, 0, 40, 0));
        EstiloMenu.agregar(this, ajustes, 1, new Insets(0, 0, 24, 0));
        EstiloMenu.agregar(this, botonVolver, 2, new Insets(0, 0, 0, 0));
    }

    public void sincronizarVolumen() {
        ajustes.sincronizarVolumen();
    }

    public void actualizarModoPantalla(boolean pantallaCompleta) {
        ajustes.actualizarModoPantalla(pantallaCompleta);
    }
}
