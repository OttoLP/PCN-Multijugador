-- ATENCION: elimina todos los usuarios actuales y crea las cuatro cuentas de ejemplo.
CREATE DATABASE IF NOT EXISTS restordb;
USE restordb;
DROP TABLE IF EXISTS usuarios;
CREATE TABLE usuarios (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    nombre_usuario VARCHAR(50) NOT NULL UNIQUE,
    contrasena VARCHAR(100) NOT NULL
);

INSERT INTO usuarios (nombre_usuario, contrasena)
VALUES
    ('Jugador1', 'Player1'),
    ('Jugador2', 'Player2'),
    ('Jugador3', 'Player3'),
    ('Jugador4', 'Player4');
