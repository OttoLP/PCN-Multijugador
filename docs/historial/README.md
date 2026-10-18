# Historial de entregas

Aquí se conserva la documentación de las entregas anteriores, tal como se entregó.
**El código de cada entrega no está en carpetas**: Git lo guarda en un *tag*, así
`main` solo tiene el código vigente.

| Entrega | Qué fue | Documentación | Código (tag) |
|---------|---------|---------------|--------------|
| Parcial 1 | Chat multiusuario con autenticación en MySQL (consola) | [`parcial-1/`](parcial-1/) | `parcial-1` |
| Parcial 2 | Juego gráfico multijugador (hasta 4 jugadores) que reemplaza al chat | [`parcial-2/`](parcial-2/) | `parcial-2` |

## Contenido

- `parcial-1/Manual.pdf` y `Manual.docx`: manual de la práctica (UASLP, Facultad de Ingeniería).
  Autores: Jacobo Rodríguez Torres, Alberto Otoniel López Romero, José Orlando Juárez Ramírez,
  Eloy Emilio Hernández Hernández, Karla Dayana Mejía Padrón y Francisco Abundis Ávalos.
- `parcial-1/README-parcial1.md` y `restordb.sql`: README y script de base de datos originales del chat.
- `parcial-2/README-parcial2.md`: README original del juego. Menciona rutas y archivos de aquella
  entrega (por ejemplo `ChatMultiusuario-Servidor/`); la guía vigente está en el
  [README](../../README.md) y en [`docs/INSTALACION.md`](../INSTALACION.md).

## Ver el código de una entrega anterior

```bash
git checkout parcial-1     # el chat
git checkout parcial-2     # el juego de esa entrega
git checkout main          # volver al código actual
```

En GitHub también aparecen en **Releases / Tags**.
