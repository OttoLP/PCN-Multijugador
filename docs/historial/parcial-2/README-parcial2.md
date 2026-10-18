# PCN multijugador — hasta 4 jugadores

Integración del PCN v5 adjunto con la conexión por sockets/JSON y la autenticación del ChatMultiusuario. Cada cliente mueve su personaje con **WASD** y dibuja a los otros jugadores. El servidor valida el acceso, asigna un lugar y distribuye coordenadas. No dibuja ni simula el juego.

## 1. Qué incluye

- `PCN-Cliente`: el juego gráfico, con sus paquetes `main`, `entidad`, `tile`, sus imágenes originales y un paquete `red` con las clases de red adaptadas del chat y `ConexionServidor`.
- `ChatMultiusuario-Servidor`: las siete clases del servidor del chat, adaptadas para posiciones.
- `database/usuarios.sql`: la tabla simplificada de tres columnas y las cuatro cuentas solicitadas. **Borra las cuentas anteriores**.
- `docs/PROTOCOLO-Y-CODIGO.md`: explicación de JSON y de dónde modificar cada comportamiento.
- `docs/vista-jugadores.png`: dibujo generado por el propio GamePanel durante las pruebas.
- `docs/PRUEBAS.md`: qué se verificó y qué falta comprobar en tu VM.

Se mantiene la ventana de **26 columnas × 15 renglones**, tiles de 48 píxeles y resolución de 1248 × 720. El ZIP de PCN v5 recibido solamente tiene fondo de pasto: esta versión conserva ese fondo y no agrega mapa externo ni colisiones con tiles o jugadores. Se añadieron límites para no salir de la ventana.

## 2. Requisitos

- JDK **17 o superior** (incluido JDK 25). Comprueba `java -version` y `javac -version`.
- MySQL en el equipo del servidor o accesible desde él.
- Para NetBeans: abrir los dos proyectos Maven. La primera compilación requiere internet para resolver dependencias y plugins.
- También hay scripts sin Maven y los JAR de Gson/MySQL en `lib/`, para compilar sin descargar dependencias.

No ejecutes el servidor original del chat a la vez que este servidor: ambos usan el puerto 5555.

## 3. Base de datos simplificada

Abre `database/usuarios.sql` en MySQL Workbench y ejecútalo. **El script borra la tabla usuarios y sus cuentas actuales** y crea una tabla con solo `id_usuario`, `nombre_usuario` y `contrasena`, tal como se solicitó. No lo ejecutes para iniciar cada partida: es un paso de configuración.

Se usa `USE restordb;` (MySQL no utiliza `USE DATABASE restordb;`). `DROP TABLE IF EXISTS` también permite la instalación cuando la tabla todavía no existe.

| Usuario | Contraseña |
|---|---|
| Jugador1 | Player1 |
| Jugador2 | Player2 |
| Jugador3 | Player3 |
| Jugador4 | Player4 |

En `UsuarioDAO` se eliminó `AND activo = TRUE`, porque esa columna ya no existe. La consulta compara únicamente nombre y contraseña. Ya no hay cuentas activas/inactivas.

El ID de la tabla no es el lugar/color de la partida: el servidor asigna un índice 0–3 al conectar. No necesitas guardar colores ni coordenadas en MySQL. El límite de cuatro corresponde a sesiones simultáneas; el servidor sigue rechazando una segunda sesión del mismo usuario.

## 4. Configurar el servidor en la VM

Edita `ChatMultiusuario-Servidor/src/main/java/com/mycompany/chatmultiusuario/ConectarMySQL.java`, igual que en el chat original:

```java
private static final String URL =
        "jdbc:mysql://localhost:3306/restordb"
        + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
private static final String USER = "TU_USUARIO";
private static final String PASSWORD = "TU_CONTRASENIA$";
```

Sustituye USER y PASSWORD por los datos de tu MySQL y vuelve a compilar. La conexión del servidor no utiliza archivos properties; solamente los clientes leen sus credenciales desde un archivo.

Aquí van las credenciales de **MySQL**, no las de Jugador1/Jugador2. Si MySQL está en la misma VM que el servidor Java, deja `localhost`. Si está en otro equipo, sustituye únicamente ese host por su IP y configura el acceso correspondiente en MySQL.

En NetBeans:

1. Archivo → Abrir proyecto → selecciona `ChatMultiusuario-Servidor` (la carpeta que tiene `pom.xml`).
2. Limpiar y construir.
3. Ejecutar proyecto. Si pregunta la clase principal, elige `com.mycompany.chatmultiusuario.ChatMultiusuario`.
4. Debe aparecer: `Servidor PCN en puerto 5555. Maximo 4 jugadores.`

La conexión se realiza mediante `DriverManager.getConnection(URL, USER, PASSWORD)`, como en el proyecto original.

## 5. Configurar cada cliente

El cliente se ejecuta en tu computadora anfitriona (**HOST**) y el servidor Java en la **VM**.

Primero abre `PCN-Cliente/src/red/ConexionServidor.java` y sustituye el marcador por la IP real de la VM:

```java
private static final String IP_SERVIDOR = "IP_DE_LA_VM";
private static final int PUERTO = 5555;
```

No dejes el marcador ni uses `localhost` para conectarte desde el host a la VM: `localhost` apuntaría al propio host. La VM debe ser accesible desde el host (por ejemplo, red puente) y permitir TCP 5555. Después de cambiar la IP, recompila.

El archivo `PCN-Cliente/cliente.properties` contiene únicamente la autenticación:

```properties
usuario=Jugador1
password=Player1
```

`ConexionServidor` abre el socket. `Cliente` recibe ese socket y se encarga de autenticar, recibir estados y enviar coordenadas. `Game` coordina esas llamadas y abre la ventana.

No se escriben credenciales en terminal y no hay formulario de login. El programa lee el archivo, envía el JSON de autenticación y abre la ventana si el servidor acepta. Si hay un error, muestra un cuadro de diálogo.

En NetBeans abre `PCN-Cliente`, limpia/construye y ejecuta `main.Game`. Haz clic en la ventana si no tiene el foco y mueve con **W, A, S, D**.

Para probar cuatro clientes, copia la carpeta `PCN-Cliente` a cuatro equipos o usa los archivos de ejemplo con los scripts siguientes. Cada sesión debe usar un usuario distinto.

Los colores se asignan por el primer lugar libre, según el orden de conexión:

| Lugar | ID en JSON | Camisa |
|---|---|---|
| 1 | 0 | Roja |
| 2 | 1 | Azul |
| 3 | 2 | Verde |
| 4 | 3 | Amarilla |

El color no pertenece permanentemente al usuario. Al salir alguien, el próximo jugador puede recibir ese lugar/color.

## 6. Ejecutar sin Maven

Los scripts compilan primero y ejecutan después. Requieren el JDK en PATH. Las bibliotecas ya están incluidas.

**Windows:** dentro de `ChatMultiusuario-Servidor`, abre `ejecutar.bat`. Después, desde CMD en `PCN-Cliente`, ejecuta cada comando en una terminal distinta:

```bat
ejecutar.bat config/Jugador1.properties
ejecutar.bat config/Jugador2.properties
ejecutar.bat config/Jugador3.properties
ejecutar.bat config/Jugador4.properties
```

**Linux:** dentro de `ChatMultiusuario-Servidor`:

```bash
bash ejecutar.sh
```

Después, desde `PCN-Cliente`, cada uno en una terminal distinta:

```bash
bash ejecutar.sh config/Jugador1.properties
bash ejecutar.sh config/Jugador2.properties
bash ejecutar.sh config/Jugador3.properties
bash ejecutar.sh config/Jugador4.properties
```

Antes de usarlos, configura la IP de la VM en `ConexionServidor.java`. Los cuatro archivos properties contienen solo usuario/password y coinciden con las cuentas del SQL.

Sin argumento, el script del cliente usa `cliente.properties`. El argumento selecciona un archivo; las credenciales siempre se leen del archivo.

## 7. Qué esperar

- El primer jugador aparece en `(100,100)`. Los siguientes se separan 96 píxeles horizontalmente.
- Cada usuario mueve solamente su personaje. Los demás lo ven a través de las coordenadas reenviadas.
- Los nombres aparecen sobre los sprites. El pelo, la piel y el contorno mantienen su diseño; se cambia el rojo del torso en las ocho imágenes de animación al cargarlas.
- Al cerrar una ventana se cierra su socket: el servidor libera el lugar y envía el estado restante. No hace falta un tercer tipo de mensaje para salir.
- El quinto cliente autenticado recibe `Sala llena: maximo 4 jugadores.` y no abre la ventana.
- Si el servidor se cierra, el cliente muestra un aviso sobre el juego. Para reconectarte, cierra y abre el cliente.
- En una caída de red sin cierre de socket, detectar la desconexión puede tardar. No se añadió heartbeat ni reconexión automática.

## 8. Si algo falla

- **Connection refused / no se pudo entrar:** comprueba que el servidor siga ejecutándose y revisa IP_SERVIDOR y PUERTO en ConexionServidor.java. Con la VM en red puente, anfitrión y VM deben poder comunicarse; permite el puerto TCP 5555 en el firewall del servidor si está bloqueado.
- **No se pudo autenticar:** revisa primero la consola del servidor. Puede ser usuario/contraseña del juego incorrectos, un error de conexión a MySQL.
- **Access denied:** comprueba las constantes USER y PASSWORD de `ConectarMySQL.java`.
- **No encuentra .properties:** conserva el archivo junto al `pom.xml` del proyecto que ejecutas. Los scripts fijan automáticamente el directorio de trabajo.
- **package com.google.gson does not exist:** en NetBeans comprueba la descarga de Dependencies. Como alternativa, los scripts usan el JAR incluido en `lib/`.
- **javac no se reconoce / command not found:** instala/configura un JDK, no solamente un JRE.

Es una versión de práctica que conserva la autenticación del chat: contraseñas en texto en la tabla/archivo y sockets sin TLS. No incluye chat de texto, predicción, interpolación, control de velocidad del servidor ni persistencia de posiciones. El servidor confía en el movimiento del cliente y solo comprueba el rango del panel.
