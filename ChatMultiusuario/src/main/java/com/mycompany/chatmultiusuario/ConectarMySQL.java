
package com.mycompany.chatmultiusuario;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConectarMySQL {

    private static final String URL =
            // aca tambien solo se cambia el nombre de la BD
            "jdbc:mysql://localhost:3306/restordb"
            + "?useSSL=false"
            + "&allowPublicKeyRetrieval=true"
            + "&serverTimezone=UTC";
    // Aca se pone "root" y en el caso que se configuro sin ser root agrega el user
    private static final String USER = "TU_USUARIO";

    // Aqui la contrasenia que pusieron en el workbench, creo que
    // es la misma y si no pues, cambiala valedor :D
    private static final String PASSWORD = "TU_CONTRASENIA$";

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                URL,
                USER,
                PASSWORD
        );
    }
}