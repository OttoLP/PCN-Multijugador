# Cómo contribuir

Guía para trabajar en equipo sin pisarnos. Es corta; léela completa una vez.

## Flujo de trabajo

1. **Prepara tu entorno** siguiendo [`docs/INSTALACION.md`](docs/INSTALACION.md).
   Crea tu `servidor/config.properties` y tu `cliente/servidor.properties` (no se suben a git).
2. **Actualiza `main`** antes de empezar:

   ```bash
   git checkout main
   git pull
   ```

3. **Crea una rama por tarea.** Nunca trabajes directo en `main`:

   ```bash
   git checkout -b feature/colisiones-con-muros
   ```

   | Prefijo | Para |
   |---------|------|
   | `feature/` | Funcionalidad nueva |
   | `fix/` | Corrección de un error |
   | `docs/` | Solo documentación |
   | `refactor/` | Reorganizar código sin cambiar el comportamiento |

4. **Haz commits pequeños** con mensajes claros en español, en presente:
   `Agrega colisiones con muros`, `Corrige el límite derecho de la ventana`.
5. **Sube tu rama y abre un Pull Request** hacia `main`:

   ```bash
   git push -u origin feature/colisiones-con-muros
   ```

   Llena la plantilla del PR y pide la revisión de al menos una persona del equipo.
6. **Cuando lo aprueben**, haz *Squash and merge* y borra la rama.

## Antes de abrir un Pull Request

- [ ] El servidor compila y arranca (`mvn compile exec:java` en `servidor/`).
- [ ] El cliente compila y abre (`ejecutar.sh` o `ejecutar.bat` en `cliente/`).
- [ ] Probaste con **al menos 2 clientes** conectados al mismo tiempo.
- [ ] Si cambiaste el formato de los mensajes, actualizaste `Mensaje.java` en **cliente y servidor** y [`docs/PROTOCOLO.md`](docs/PROTOCOLO.md).
- [ ] Si cambiaste la base de datos, actualizaste `database/usuarios.sql` y lo avisaste al equipo.
- [ ] Hiciste `git pull` de `main` y resolviste los conflictos en tu rama.

## Qué NO subir

- `servidor/config.properties` y `cliente/servidor.properties` (configuración personal).
- Contraseñas o IPs reales fuera de los archivos de ejemplo.
- Carpetas generadas: `build/`, `bin/`, `target/`, `out/`, archivos `.class`.
- `.jar` nuevos. La única excepción es `cliente/lib/` (el cliente se ejecuta sin Maven).
- Configuración de tu IDE (`.idea/`, `nbproject/private/`, `.vscode/`).

El `.gitignore` ya cubre casi todo esto; si `git status` te muestra algo de esa lista, no lo agregues.

## Resolver conflictos

```bash
git fetch origin
git merge origin/main        # en tu rama; arregla los archivos marcados y haz commit
```

Si dos personas tocan el mismo archivo, avísense en el grupo antes de reorganizarlo.

## Estilo de código

- Nombres de clases, métodos y variables en español, como en el resto del proyecto.
- Sigue el estilo del archivo que editas (llaves, indentación de 4 espacios).
- Comenta el *porqué* de lo que no sea obvio.

## Tareas y errores

Usa los **Issues** de GitHub: hay plantillas para tareas y para errores. Asígnate el Issue antes de empezar
para que nadie más haga lo mismo.

## Para quien administra el repositorio

Configurar una sola vez en GitHub:

1. **Settings → Collaborators:** agrega a cada integrante del equipo.
2. **Settings → Branches** (o **Rules → Rulesets**): protege `main` exigiendo Pull Request con al menos
   1 aprobación y bloqueando los *force push*.
3. **Settings → General → Pull Requests:** activa *Squash merging* y *Automatically delete head branches*.
4. Al cerrar cada entrega, crea un tag para poder volver a ese estado:

   ```bash
   git tag -a parcial-3 -m "Entrega parcial 3"
   git push origin parcial-3
   ```
