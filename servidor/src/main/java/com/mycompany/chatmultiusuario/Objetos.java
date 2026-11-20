package com.mycompany.chatmultiusuario;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

// Objetos del mapa: barriles que los jugadores rompen con su ataque automatico y lo que sueltan
// (corazon, furia, rapidez). No bloquean el paso. Se usa dentro de synchronized (clientes).
final class Objetos {
    static final String BARRIL = "barril";
    static final String CORAZON = "corazon";
    static final String FURIA = "furia";
    static final String RAPIDEZ = "rapidez";

    private static final int CUERPO = Mapa.CUERPO;
    private static final int RECOGER = 12;          // margen alrededor del jugador para tomar un objeto
    private static final int DISTANCIA_JUGADOR = 150; // un barril nuevo no sale encima de un jugador

    private static final class Objeto {
        final String tipo;
        final Rectangle cuerpo;
        final int vidaMax;
        final long expira;   // 0 = no caduca (barriles)
        int vida;

        Objeto(String tipo, Rectangle cuerpo, int vida, long expira) {
            this.tipo = tipo;
            this.cuerpo = cuerpo;
            this.vida = vida;
            this.vidaMax = vida;
            this.expira = expira;
        }
    }

    private final Mapa mapa;
    private final List<Objeto> lista = new ArrayList<>();
    private long proximaReposicion;

    Objetos(Mapa mapa) {
        this.mapa = mapa;
    }

    void reiniciar() {
        lista.clear();
        proximaReposicion = 0;
    }

    int barriles() {
        int total = 0;
        for (Objeto objeto : lista) {
            if (objeto.tipo.equals(BARRIL)) total++;
        }
        return total;
    }

    int sueltos() {
        return lista.size() - barriles();
    }

    // Pone un barril nuevo si faltan (a lo mas uno por Combate.REPOSICION_BARRIL_MS).
    // Solo en tiles alcanzables y secos, y lejos de los jugadores.
    boolean reponerBarril(long ahora, Mensaje[] jugadores) {
        if (ahora < proximaReposicion || barriles() >= Combate.BARRILES) return false;
        proximaReposicion = ahora + Combate.REPOSICION_BARRIL_MS;
        ThreadLocalRandom azar = ThreadLocalRandom.current();
        int margen = (Mapa.TILE - CUERPO) / 2;
        for (int intento = 0; intento < 40; intento++) {
            int col = azar.nextInt(mapa.columnas());
            int fila = azar.nextInt(mapa.filas());
            if (mapa.bloquea(fila, col) || !mapa.alcanzable(fila, col)
                    || mapa.lento(fila, col) || mapa.cura(fila, col)) continue;
            Rectangle candidato = new Rectangle(col * Mapa.TILE + margen, fila * Mapa.TILE + margen, CUERPO, CUERPO);
            if (cercaDeJugador(candidato, jugadores) || chocaConObjeto(candidato)) continue;
            lista.add(new Objeto(BARRIL, candidato, Combate.VIDA_BARRIL, 0));
            return true;
        }
        return false;
    }

    private boolean cercaDeJugador(Rectangle punto, Mensaje[] jugadores) {
        for (Mensaje jugador : jugadores) {
            if (jugador != null && Math.hypot(jugador.x - punto.x, jugador.y - punto.y) < DISTANCIA_JUGADOR) return true;
        }
        return false;
    }

    private boolean chocaConObjeto(Rectangle punto) {
        for (Objeto objeto : lista) {
            if (punto.intersects(objeto.cuerpo)) return true;
        }
        return false;
    }

    // Indice del barril mas cercano dentro del alcance, o -1. Valido hasta el siguiente cambio de la lista.
    int barrilMasCercano(int x, int y, int alcance) {
        int mejor = -1;
        long menor = (long) alcance * alcance;
        int cx = x + CUERPO / 2;
        int cy = y + CUERPO / 2;
        for (int i = 0; i < lista.size(); i++) {
            Objeto objeto = lista.get(i);
            if (!objeto.tipo.equals(BARRIL)) continue;
            long dx = objeto.cuerpo.x + CUERPO / 2 - cx;
            long dy = objeto.cuerpo.y + CUERPO / 2 - cy;
            long distancia = dx * dx + dy * dy;
            if (distancia <= menor) {
                menor = distancia;
                mejor = i;
            }
        }
        return mejor;
    }

    Rectangle posicion(int i) {
        return lista.get(i).cuerpo;
    }

    // Resta vida a un barril. Si se rompe, lo quita y puede soltar un objeto. Devuelve true si se rompio.
    boolean danarBarril(int i, int cantidad, long ahora) {
        Objeto barril = lista.get(i);
        barril.vida -= cantidad;
        if (barril.vida > 0) return false;
        lista.remove(i);
        String suelta = tirada(ThreadLocalRandom.current().nextInt(100));
        if (suelta != null) lista.add(new Objeto(suelta, barril.cuerpo, 0, ahora + Combate.OBJETO_VIDA_MS));
        return true;
    }

    // Que suelta un barril segun un numero de 0 a 99 (null = nada).
    static String tirada(int numero) {
        if (numero < Combate.PROBABILIDAD_CORAZON) return CORAZON;
        if (numero < Combate.PROBABILIDAD_CORAZON + Combate.PROBABILIDAD_FURIA) return FURIA;
        if (numero < Combate.PROBABILIDAD_CORAZON + Combate.PROBABILIDAD_FURIA + Combate.PROBABILIDAD_RAPIDEZ) return RAPIDEZ;
        return null;
    }

    // El jugador toma un objeto suelto que toca. Un corazon no se toma si no hace falta.
    // Devuelve el tipo tomado o null.
    String recoger(int x, int y, boolean vidaCompleta) {
        Rectangle jugador = new Rectangle(x, y, CUERPO, CUERPO);
        jugador.grow(RECOGER, RECOGER);
        for (int i = 0; i < lista.size(); i++) {
            Objeto objeto = lista.get(i);
            if (objeto.tipo.equals(BARRIL)) continue;
            if (objeto.tipo.equals(CORAZON) && vidaCompleta) continue;
            if (jugador.intersects(objeto.cuerpo)) {
                lista.remove(i);
                return objeto.tipo;
            }
        }
        return null;
    }

    // Quita los objetos sueltos que ya caducaron. Devuelve true si quito alguno.
    boolean limpiar(long ahora) {
        return lista.removeIf(objeto -> objeto.expira != 0 && objeto.expira <= ahora);
    }

    Mensaje[] posiciones() {
        Mensaje[] posiciones = new Mensaje[lista.size()];
        for (int i = 0; i < posiciones.length; i++) {
            Objeto objeto = lista.get(i);
            Mensaje posicion = new Mensaje();
            posicion.tipo = objeto.tipo;
            posicion.x = objeto.cuerpo.x;
            posicion.y = objeto.cuerpo.y;
            if (objeto.tipo.equals(BARRIL)) {
                posicion.vida = objeto.vida;
                posicion.vidaMax = objeto.vidaMax;
            }
            posiciones[i] = posicion;
        }
        return posiciones;
    }
}
