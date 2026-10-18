CREATE DATABASE IF NOT EXISTS restordb

USE restordb;

CREATE TABLE usuarios (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    nombre_usuario VARCHAR(50) NOT NULL UNIQUE,
    contrasena VARCHAR(100) NOT NULL,
    nombre_completo VARCHAR(100),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO usuarios
    (nombre_usuario, contrasena, nombre_completo)
VALUES
    ('otto', '12345', 'Otoniel López'),
    ('ana', 'ana123', 'Ana Martínez'),
    ('carlos', 'carlos123', 'Carlos Hernández');

SELECT * FROM usuarios;