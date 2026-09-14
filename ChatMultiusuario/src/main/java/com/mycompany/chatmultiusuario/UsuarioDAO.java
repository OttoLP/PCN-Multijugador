package com.mycompany.chatmultiusuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsuarioDAO {

    public boolean autenticar(
            String usuario,
            String contrasena
    ) {
        String consulta = """
                SELECT id_usuario
                FROM usuarios
                WHERE nombre_usuario = ?
                AND contrasena = ?
                AND activo = TRUE
                """;

        ConectarMySQL conexionBD = new ConectarMySQL();

        try (
            Connection conexion =
                    conexionBD.getConnection();

            PreparedStatement sentencia =
                    conexion.prepareStatement(consulta)
        ) {
            sentencia.setString(1, usuario);
            sentencia.setString(2, contrasena);

            try (ResultSet resultado =
                         sentencia.executeQuery()) {

                return resultado.next();
            }

        } catch (SQLException e) {
            System.out.println(
                    "Error al autenticar: "
                    + e.getMessage()
            );

            return false;
        }
    }
}