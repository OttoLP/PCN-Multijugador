# Chat multiusuario con autenticación

Aplicación cliente-servidor desarrollada en Java que permite la
comunicación entre múltiples usuarios mediante sockets.

Antes de entrar al chat, los usuarios deben autenticarse utilizando
credenciales almacenadas en una base de datos MySQL.

## Arquitectura

Host (Cliente) → VM (Servidor) → MySQL (restordb)

- El cliente solicita el usuario y la contraseña.
- El servidor recibe las credenciales.
- El servidor consulta la base de datos MySQL.
- Si las credenciales son correctas, el cliente accede al chat.
- Si son incorrectas, la conexión es rechazada.

## Estructura del repositorio

- `ChatMultiusuario`: proyecto correspondiente al servidor.
- `ConectaChatMultiusuario`: proyecto correspondiente al cliente.
- `database/restordb.sql`: script para crear la tabla y los usuarios.

## Tecnologías utilizadas

- Java
- Maven
- MySQL
- JDBC
- Sockets TCP
- Programación multihilo
- NetBeans
- Máquina virtual con red en modo puente

## Requisitos

- JDK instalado.
- Apache Maven.
- MySQL Server.
- MySQL Connector/J.
- NetBeans o un IDE compatible con Maven.
- Máquina virtual configurada en modo puente.

## Base de datos

La base de datos utilizada se llama `restordb`.

Para crear las tablas y registros de prueba se debe ejecutar:

`database/restordb.sql`

Después se deben colocar las credenciales locales de MySQL en la
clase `ConectarMySQL`.

## Configuración del servidor

En la clase `ConectarMySQL` se deben configurar los datos de acceso:

```java
private static final String USER = "TU_USUARIO_MYSQL";
private static final String PASSWORD = "TU_CONTRASENA_MYSQL";