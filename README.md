# PCN Multijugador

Juego 2D multijugador en **Java** (Swing) para hasta **4 jugadores**. Cada jugador se
autentica con usuario y contraseña guardados en **MySQL**, mueve su personaje con
**W A S D** y ve a los demás en tiempo real. El servidor valida el acceso, asigna un
lugar y distribuye las coordenadas; no dibuja ni simula el juego.

Proyecto de **Cómputo en la Nube** (UASLP, Facultad de Ingeniería). Empezó como un chat
multiusuario (parcial 1) y evolucionó a este juego (parcial 2). La documentación de esas
entregas está en [`docs/historial/`](docs/historial/).

## Arquitectura

```mermaid
flowchart LR
    subgraph HOST["Equipo anfitrión"]
        C1["Cliente 1<br/>juego "]
        C2["Cliente N<br/>hasta 4"]
    end
    subgraph VM["Máquina virtual"]
        S["Servidor Java<br/>"]
        DB[("MySQL<br/>nameDATABASE")]
    end
    C1 -- "JSON por TCP" --> S
    C2 -- "JSON por TCP" --> S
    S -- JDBC --> DB
```

- El cliente nunca habla con MySQL: envía sus credenciales al servidor y este consulta la base.
- Se intercambian mensajes JSON, uno por línea. Detalle en [`docs/PROTOCOLO.md`](docs/PROTOCOLO.md).
- El color del jugador depende del lugar que recibe al conectarse (rojo, azul, verde, amarillo), no del usuario.

## Estructura del repositorio

```
.
├── servidor/               Proyecto Maven (NetBeans), corre en la VM
│   ├── pom.xml
│   ├── config.example.properties   → copiar como config.properties
│   └── src/main/java/com/mycompany/chatmultiusuario/
├── cliente/                Juego Swing, se ejecuta sin Maven en el host
│   ├── src/                main, entidad, tile, red + sprites y tiles
│   │   └── escenarios/     mundo.txt (el mapa) y tiles.txt (qué hace cada tile)
│   ├── lib/gson-2.14.0.jar
│   ├── cliente.properties          usuario y contraseña del jugador
│   ├── config/Jugador1..4.properties
│   ├── servidor.properties.example → copiar como servidor.properties
│   └── ejecutar.sh / ejecutar.bat
├── database/usuarios.sql   Crea la tabla y las 4 cuentas de prueba
├── herramientas/           Script para recolorear los tiles y sus originales
├── docs/                   Instalación, protocolo e historial de entregas
├── CONTRIBUTING.md         Cómo trabajar en equipo
└── .github/                Plantillas de Pull Request e Issues
```

## Requisitos

- **JDK 17 o superior** (`java -version` y `javac -version`).
- **MySQL** en la VM (o accesible desde ella).
- **Servidor:** NetBeans o Maven (descarga las dependencias en la primera compilación).
- **Cliente:** solo el JDK; Gson ya viene en `cliente/lib/`.
- Una VM con red en **modo puente** que permita TCP 5555.

## Inicio rápido

**1. Base de datos** (en la VM). Ojo: el script borra la tabla `usuarios` y la recrea.

```bash
mysql -u root -p < database/usuarios.sql
```

**2. Servidor** (en la VM)

```bash
cd servidor
cp config.example.properties config.properties   # escribe tu usuario y contraseña de MySQL
mvn compile exec:java                             # o ejecuta el proyecto desde NetBeans
```

Debe mostrar `Conexion a MySQL correcta.` y `Servidor PCN en puerto 5555. Maximo 4 jugadores.`

**3. Cliente** (en el host, una terminal por jugador)

```bash
cd cliente
cp servidor.properties.example servidor.properties   # escribe la IP de la VM
bash ejecutar.sh config/Jugador1.properties          # Windows: ejecutar.bat config\Jugador1.properties
```

Repite con `Jugador2`, `Jugador3` y `Jugador4` para tener más jugadores.
Guía completa y solución de problemas: [`docs/INSTALACION.md`](docs/INSTALACION.md).

## Cuentas de prueba

| Usuario | Contraseña |
|---------|------------|
| Jugador1 | Player1 |
| Jugador2 | Player2 |
| Jugador3 | Player3 |
| Jugador4 | Player4 |

Solo para desarrollo. Cada usuario puede tener una sola sesión a la vez.

## Configuración local (no se sube a git)

| Archivo | Contiene | Plantilla |
|---------|----------|-----------|
| `servidor/config.properties` | Datos de MySQL | `config.example.properties` |
| `cliente/servidor.properties` | IP de la VM | `servidor.properties.example` |

Cada integrante crea los suyos, así nadie pisa la configuración de otro.

## Cambiar el aspecto

**Mapa.** Los colores salen de `herramientas/recolorear_tiles.py`, que parte de los originales en
`herramientas/tiles-originales/`. Para otro ambiente, cambia `PALETA` y ejecuta (requiere `pip install pillow`):

```bash
python herramientas/recolorear_tiles.py
```

Las propiedades de cada tile (bloquea, pantano, fuente) están en `cliente/src/escenarios/tiles.txt`.

**Jugador (mago).** `cliente/src/spritesjugador/mago.png` es una hoja de 4 × 6 cuadros de 16 × 16 px. Cada fila es una
dirección, en este orden: abajo, derecha, izquierda y arriba, con 4 cuadros de caminata (el primero es de pie); las
filas 5 y 6 no se usan todavía. Solo cambia por jugador el azul del sombrero y la túnica (`TUNICAS` en `Jugador.java`:
rojo, azul, verde y amarillo). Para otro personaje, reemplaza la hoja respetando ese orden y esos tres azules.

**Monstruo (Ocular Watcher).** `cliente/src/spritesmonstruo/monstruo.png` son 4 cuadros de 16 × 16 px en una fila,
a 200 ms cada uno. El archivo editable está en `herramientas/sprites-originales/OcularWatcher.aseprite`.

## Documentación

- [Instalación y ejecución](docs/INSTALACION.md)
- [Protocolo cliente-servidor](docs/PROTOCOLO.md)
- [Cómo contribuir](CONTRIBUTING.md)
- [Historial de entregas (parcial 1 y 2)](docs/historial/)

## Limitaciones conocidas

- Las contraseñas están en texto plano en la base de datos y los sockets no usan TLS.
- El servidor confía en el movimiento que calcula el cliente; solo valida los límites del mundo. La vida, el daño y los monstruos sí los decide el servidor.
- Todos los monstruos son del mismo tipo, y los barriles y objetos sueltos no bloquean el paso. Sin reconexión automática.
- Quien entra a la mitad de una partida empieza sin mejoras, aunque los demás ya tengan varias.
- No hay chat de texto en esta versión.

Son buenos candidatos para las siguientes entregas: abre un Issue por cada uno.
