# Instalación y ejecución

## Qué corre dónde

| Parte | Dónde | Cómo |
|-------|-------|------|
| MySQL (`restordb`) | VM | Se carga con `database/usuarios.sql` |
| Servidor | VM | NetBeans o Maven (`servidor/pom.xml`) |
| Cliente | Equipo anfitrión (host) | Scripts `ejecutar.sh` / `ejecutar.bat`, sin Maven |

## 1. Base de datos

Abre `database/usuarios.sql` en MySQL Workbench y ejecútalo, o desde terminal:

```bash
mysql -u root -p < database/usuarios.sql
```

> **El script elimina la tabla `usuarios` y todas sus cuentas** y crea las cuatro cuentas de prueba.
> Es un paso de configuración: no lo ejecutes antes de cada partida.

## 2. Servidor (en la VM)

1. Copia `servidor/config.example.properties` como `servidor/config.properties` y escribe tus datos:

   ```properties
   db.host=localhost
   db.port=3306
   db.name=restordb
   db.user=root
   db.password=tu_contraseña
   ```

   Son las credenciales de **MySQL**, no las de Jugador1, Jugador2, etc. Si MySQL corre en la misma VM
   que el servidor Java, deja `localhost`. Este archivo no se sube a git.

2. Ejecuta el servidor, con una de estas dos opciones:

   - **NetBeans:** Archivo → Abrir proyecto → carpeta `servidor` → Limpiar y construir → Ejecutar.
     Si pregunta la clase principal: `com.mycompany.chatmultiusuario.ChatMultiusuario`.
   - **Terminal:**

     ```bash
     cd servidor
     mvn compile exec:java
     ```

3. Debe imprimir:

   ```
   Conexion a MySQL correcta.
   Servidor PCN en puerto 5555. Maximo 4 jugadores.
   ```

   Si no puede conectarse a MySQL, el servidor lo dice y se detiene.

4. Permite el puerto TCP 5555 en el firewall de la VM si hace falta (por ejemplo `sudo ufw allow 5555/tcp`)
   y obtén su IP con `ip a` (Linux) o `ipconfig` (Windows).

Para usar otra ruta de configuración: `-Dconfig=ruta/al/archivo.properties`.
No ejecutes dos servidores a la vez: ambos usan el puerto 5555.

## 3. Cliente (en el host)

1. Copia `cliente/servidor.properties.example` como `cliente/servidor.properties` y escribe la IP de la VM:

   ```properties
   servidor.ip=192.168.1.50
   ```

   No uses `localhost`: desde el host apuntaría a tu propio equipo, no a la VM. Este archivo no se sube a git.

2. Ejecuta desde la carpeta `cliente`. Los scripts compilan primero y ejecutan después (requieren el JDK en el PATH):

   ```bash
   # Linux / macOS
   bash ejecutar.sh config/Jugador1.properties
   ```

   ```bat
   :: Windows (CMD)
   ejecutar.bat config\Jugador1.properties
   ```

   Sin argumento usan `cliente.properties`. El archivo indica con qué usuario entrar:

   ```properties
   usuario=Jugador1
   password=Player1
   ```

3. Mueve tu personaje con **W, A, S, D**. Si no responde, haz clic en la ventana.

**Varios jugadores:** abre una terminal por jugador y usa `Jugador1`, `Jugador2`, `Jugador3` y `Jugador4`.
Cada sesión debe usar un usuario distinto.

**Desde VS Code o NetBeans (sin los scripts):** el directorio de trabajo debe ser `cliente/` (ahí se buscan
`cliente.properties` y `servidor.properties`) y `lib/gson-2.14.0.jar` debe estar en el classpath.
La clase principal es `main.Game`.

## 4. Qué esperar

- El primer jugador aparece en `(100, 100)` y los siguientes se separan 96 píxeles.
- Los colores se asignan por el primer lugar libre: rojo, azul, verde y amarillo.
- Al cerrar una ventana se libera su lugar.
- El quinto jugador recibe `Sala llena: maximo 4 jugadores.`
- Si el servidor se cierra, el cliente muestra un aviso. Para reconectar, cierra y abre el cliente.

## 5. Si algo falla

| Síntoma | Causa probable y solución |
|---------|---------------------------|
| `No existe servidor.properties` | Falta copiar `servidor.properties.example` como `servidor.properties` |
| `Escribe la IP real de tu VM` | `servidor.ip` sigue con el valor de ejemplo |
| `Connection refused` / `connect timed out` | Servidor apagado, IP incorrecta, firewall bloqueando el 5555 o VM sin red en modo puente |
| El servidor: `No se pudo leer config.properties` | Falta copiar `config.example.properties` como `config.properties` en `servidor/` |
| El servidor: `Access denied` | `db.user` o `db.password` no coinciden con tu MySQL |
| El servidor: `Communications link failure` | MySQL apagado o `db.host`/`db.port` incorrectos |
| `No se pudo autenticar` | Usuario o contraseña del `.properties` del cliente incorrectos; revisa la consola del servidor |
| `Ese usuario ya esta conectado` | Ese jugador ya tiene una sesión abierta |
| `javac` no se reconoce | Instala un JDK (no solo el JRE) y agrégalo al PATH |
| `release version 17 not supported` | Tu JDK es anterior al 17 |
