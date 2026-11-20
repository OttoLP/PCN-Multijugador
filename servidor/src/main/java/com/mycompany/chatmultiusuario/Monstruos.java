package com.mycompany.chatmultiusuario;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

final class Monstruos {
    private static final int TILE = Mapa.TILE;
    private static final int CUERPO = Mapa.CUERPO;
    private static final int CONTACTO = 8;   // margen alrededor del jugador para contar un golpe
    private static final int PASO = 5;
    private static final int PASO_LENTO = 3;   // en el pantano avanzan al 60 %
    private final Mapa mapa;
    private final int[][] distancia;   // tiles hasta el jugador vivo mas cercano (se recalcula cada ciclo)

    private static final class Enemigo {
        final Rectangle cuerpo;
        final int vidaMax;
        int vida;

        Enemigo(Rectangle cuerpo, int vida) {
            this.cuerpo = cuerpo;
            this.vida = vida;
            this.vidaMax = vida;
        }
    }

    // Solo hay enemigos vivos: al morir se quitan de la lista.
    private final List<Enemigo> enemigos = new ArrayList<>();

    Monstruos(Mapa mapa) {
        this.mapa = mapa;
        this.distancia = new int[mapa.filas()][mapa.columnas()];
    }

    void reiniciar() {
        enemigos.clear();
    }

    int vivos() {
        return enemigos.size();
    }

    // jugadores: solo los que estan vivos (los caidos no atraen ni estorban).
    // Los monstruos siguen el camino mas corto por tiles libres hasta el jugador mas cercano,
    // asi rodean muros y arboles en lugar de atorarse con ellos.
    boolean update(Mensaje[] jugadores) {
        int[] filasOrigen = new int[jugadores.length];
        int[] colsOrigen = new int[jugadores.length];
        int cantidad = 0;
        for (Mensaje jugador : jugadores) {
            if (jugador == null) continue;
            filasOrigen[cantidad] = (jugador.y + CUERPO / 2) / TILE;
            colsOrigen[cantidad] = (jugador.x + CUERPO / 2) / TILE;
            cantidad++;
        }
        if (cantidad == 0) return false;
        mapa.calcularDistancias(filasOrigen, colsOrigen, cantidad, distancia);

        boolean cambio = false;
        for (Enemigo enemigo : enemigos) {
            Rectangle monstruo = enemigo.cuerpo;
            int[] destino = destino(monstruo, jugadores);
            int paso = mapa.lento(filaCentro(monstruo), colCentro(monstruo)) ? PASO_LENTO : PASO;
            int dx = Math.max(-paso, Math.min(paso, destino[0] - monstruo.x));
            int dy = Math.max(-paso, Math.min(paso, destino[1] - monstruo.y));
            cambio |= mover(enemigo, dx, 0, jugadores);
            cambio |= mover(enemigo, 0, dy, jugadores);
        }
        return cambio;
    }

    private int filaCentro(Rectangle cuerpo) { return (cuerpo.y + CUERPO / 2) / TILE; }
    private int colCentro(Rectangle cuerpo) { return (cuerpo.x + CUERPO / 2) / TILE; }

    // Esquina superior izquierda a la que debe dirigirse el monstruo en este ciclo.
    private int[] destino(Rectangle monstruo, Mensaje[] jugadores) {
        Mensaje objetivo = null;
        int menorDistancia = Integer.MAX_VALUE;
        for (Mensaje jugador : jugadores) {
            if (jugador == null) continue;
            int dx = jugador.x - monstruo.x;
            int dy = jugador.y - monstruo.y;
            int cuadrada = dx * dx + dy * dy;
            if (cuadrada < menorDistancia) {
                menorDistancia = cuadrada;
                objetivo = jugador;
            }
        }
        int fila = filaCentro(monstruo);
        int col = colCentro(monstruo);
        int actual = mapa.dentro(fila, col) ? distancia[fila][col] : Mapa.LEJOS;
        // Ya esta junto al jugador, o no hay camino (por ejemplo un jugador sobre un tile bloqueado): directo.
        if (actual <= 1 || actual == Mapa.LEJOS) return new int[] {objetivo.x, objetivo.y};

        int mejor = actual;
        int mejorFila = -1;
        int mejorCol = -1;
        for (int df = -1; df <= 1; df++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (df == 0 && dc == 0) continue;
                int nf = fila + df;
                int nc = col + dc;
                if (!mapa.dentro(nf, nc) || mapa.bloquea(nf, nc) || distancia[nf][nc] >= mejor) continue;
                // En diagonal solo si no se roza una esquina bloqueada.
                if (df != 0 && dc != 0 && (mapa.bloquea(fila + df, col) || mapa.bloquea(fila, col + dc))) continue;
                mejor = distancia[nf][nc];
                mejorFila = nf;
                mejorCol = nc;
            }
        }
        if (mejorFila < 0) return new int[] {objetivo.x, objetivo.y};
        int margen = (TILE - CUERPO) / 2;
        return new int[] {mejorCol * TILE + margen, mejorFila * TILE + margen};
    }

    private boolean mover(Enemigo enemigo, int dx, int dy, Mensaje[] jugadores) {
        if (dx == 0 && dy == 0) return false;
        Rectangle monstruo = enemigo.cuerpo;
        Rectangle siguiente = cuerpo(monstruo.x + dx, monstruo.y + dy);
        for (int fila = siguiente.y / TILE; fila <= (siguiente.y + CUERPO - 1) / TILE; fila++) {
            for (int col = siguiente.x / TILE; col <= (siguiente.x + CUERPO - 1) / TILE; col++) {
                if (mapa.bloquea(fila, col)) return false;
            }
        }
        for (Mensaje jugador : jugadores) {
            if (jugador != null && siguiente.intersects(cuerpo(jugador.x, jugador.y))) return false;
        }
        for (Enemigo otro : enemigos) {
            if (otro != enemigo && siguiente.intersects(otro.cuerpo)) return false;
        }
        monstruo.setLocation(siguiente.x, siguiente.y);
        return true;
    }

    // Hace aparecer un enemigo en un lugar libre, lejos de los jugadores vivos.
    // Devuelve false si no encontro lugar (se intentara de nuevo en el siguiente ciclo).
    boolean aparecer(int vida, Mensaje[] jugadores) {
        ThreadLocalRandom azar = ThreadLocalRandom.current();
        for (int intento = 0; intento < 40; intento++) {
            int col = azar.nextInt(mapa.columnas());
            int fila = azar.nextInt(mapa.filas());
            if (mapa.bloquea(fila, col) || !mapa.alcanzable(fila, col)) continue;
            Rectangle candidato = cuerpo(col * TILE, fila * TILE);
            if (!lejosDeJugadores(candidato, jugadores) || chocaConEnemigo(candidato)) continue;
            agregar(candidato.x, candidato.y, vida);
            return true;
        }
        return false;
    }

    // Coloca un enemigo exactamente en esa posicion (la usa aparecer y las pruebas).
    void agregar(int x, int y, int vida) {
        enemigos.add(new Enemigo(cuerpo(x, y), vida));
    }

    private boolean lejosDeJugadores(Rectangle punto, Mensaje[] jugadores) {
        for (Mensaje jugador : jugadores) {
            if (jugador == null) continue;
            if (Math.hypot(jugador.x - punto.x, jugador.y - punto.y) < Combate.DISTANCIA_REAPARICION) return false;
        }
        return true;
    }

    private boolean chocaConEnemigo(Rectangle punto) {
        for (Enemigo enemigo : enemigos) {
            if (punto.intersects(enemigo.cuerpo)) return true;
        }
        return false;
    }

    // Un monstruo ocupa el lugar del jugador (se usa para no dejarlo entrar en el).
    boolean choca(int x, int y) {
        return chocaConEnemigo(cuerpo(x, y));
    }

    // Un monstruo esta tocando al jugador: cuenta como golpe.
    boolean toca(int x, int y) {
        Rectangle jugador = cuerpo(x, y);
        jugador.grow(CONTACTO, CONTACTO);
        return chocaConEnemigo(jugador);
    }

    // Indice del monstruo mas cercano dentro del alcance, o -1.
    int masCercano(int x, int y, int alcance) {
        int mejor = -1;
        long menor = (long) alcance * alcance;
        int cx = x + CUERPO / 2;
        int cy = y + CUERPO / 2;
        for (int i = 0; i < enemigos.size(); i++) {
            Rectangle cuerpo = enemigos.get(i).cuerpo;
            long dx = cuerpo.x + CUERPO / 2 - cx;
            long dy = cuerpo.y + CUERPO / 2 - cy;
            long distancia = dx * dx + dy * dy;
            if (distancia <= menor) {
                menor = distancia;
                mejor = i;
            }
        }
        return mejor;
    }

    // Los indices solo valen hasta el siguiente danar(): usalos de inmediato.
    Rectangle posicion(int i) {
        return enemigos.get(i).cuerpo;
    }

    // Resta vida. Devuelve true si el monstruo murio (y lo quita de la lista).
    boolean danar(int i, int cantidad) {
        Enemigo enemigo = enemigos.get(i);
        enemigo.vida -= cantidad;
        if (enemigo.vida > 0) return false;
        enemigos.remove(i);
        return true;
    }

    Mensaje[] posiciones() {
        Mensaje[] posiciones = new Mensaje[enemigos.size()];
        for (int i = 0; i < posiciones.length; i++) {
            Enemigo enemigo = enemigos.get(i);
            Mensaje posicion = new Mensaje();
            posicion.x = enemigo.cuerpo.x;
            posicion.y = enemigo.cuerpo.y;
            posicion.vida = enemigo.vida;
            posicion.vidaMax = enemigo.vidaMax;
            posiciones[i] = posicion;
        }
        return posiciones;
    }

    private static Rectangle cuerpo(int x, int y) {
        return new Rectangle(x, y, CUERPO, CUERPO);
    }
}
