# Protocolo cliente-servidor

Comunicación por **TCP** (puerto **5555**). Cada mensaje es un objeto **JSON en una sola línea**,
terminado en salto de línea, codificado en UTF-8. Los campos `null` no se envían.
El mismo formato lo definen las clases `Mensaje` y `ConversorJSON` del cliente y del servidor
(mantén ambas copias iguales).

## Campos de `Mensaje`

| Campo | Tipo | Uso |
|-------|------|-----|
| `tipo` | texto | `autenticar` o `posiciones`. Obligatorio |
| `usuario` | texto | Nombre de usuario |
| `password` | texto | Contraseña (solo en el login del cliente) |
| `ok` | booleano | Resultado del login |
| `contenido` | texto | Mensaje legible del servidor |
| `id` | entero | Lugar del jugador, 0 a 3 (define el color) |
| `x`, `y` | enteros | Posición en píxeles |
| `jugadores` | arreglo de 4 | Estado de todos los lugares |

## 1. Autenticación

El cliente abre el socket y envía **como primer mensaje**:

```json
{"tipo":"autenticar","usuario":"Jugador1","password":"Player1"}
```

El servidor responde:

```json
{"tipo":"autenticar","usuario":"Jugador1","ok":true,"contenido":"Bienvenido","id":0,"x":100,"y":100}
```

o, si no entra:

```json
{"tipo":"autenticar","ok":false,"contenido":"Ese usuario ya esta conectado."}
```

Motivos de rechazo: faltan datos, credenciales incorrectas, usuario ya conectado o sala llena
(máximo 4 jugadores). Tras un rechazo el servidor cierra la conexión.
El servidor espera el login un máximo de 30 segundos.

## 2. Juego

El cliente envía su posición cada vez que se mueve:

```json
{"tipo":"posiciones","x":148,"y":100}
```

El servidor responde a **todos** los jugadores con el estado completo. Los lugares vacíos van como `null`:

```json
{"tipo":"posiciones","jugadores":[{"usuario":"Jugador1","id":0,"x":148,"y":100},{"usuario":"Jugador2","id":1,"x":196,"y":100},null,null]}
```

Reglas del servidor:

- Posición inicial: `x = 100 + id * 96`, `y = 100`.
- Rango válido: `x` de 0 a 1200 y `y` de 0 a 672 (ventana de 1248 × 720 con sprites de 48 px).
- Usa siempre el `id` de la conexión, nunca uno enviado por el cliente.
- Un mensaje fuera del protocolo o fuera de rango **cierra la conexión** de ese cliente.

## 3. Salida

No hay mensaje de salida: cuando el cliente cierra el socket, el servidor libera el lugar y
envía el estado actualizado a los demás.

## Cómo ampliar el protocolo

1. Agrega los campos nuevos a `Mensaje` en **cliente y servidor**.
2. Define un nuevo valor de `tipo` y manéjalo en `ManejadorClientes` (servidor) y `Cliente` (cliente).
3. Documenta el mensaje aquí en el mismo Pull Request.
