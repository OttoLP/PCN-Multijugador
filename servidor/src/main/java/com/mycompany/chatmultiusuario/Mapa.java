package com.mycompany.chatmultiusuario;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Scanner;

// El mundo: tiles de 48 px segun escenarios/mundo.txt y propiedades de cada tile segun
// escenarios/tiles.txt (los mismos archivos que lee el cliente).
final class Mapa {
    static final int TILE = 48;
    static final int FILAS = 50;
    static final int COLUMNAS = 50;
    static final int CUERPO = 36;       // lado del cuerpo de jugadores y monstruos
    static final int LEJOS = Integer.MAX_VALUE;
    private static final int MAX_CODIGOS = 32;
    // Lugares de salida de los 4 jugadores: [fila, columna] sobre el camino junto a la fuente.
    private static final int[][] INICIOS = {{21, 19}, {21, 20}, {21, 26}, {21, 27}};
    private static final int[][] VECINOS = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

    private final int[][] codigos;
    private final boolean[] bloquea;
    private final boolean[] lento;
    private final boolean[] cura;
    private final int[][] inicios;
    private final boolean[][] alcanzable;

    // inicios: [fila, columna] de los lugares de salida; solo se puede aparecer en lo que se alcanza desde ellos.
    Mapa(int[][] codigos, boolean[] bloquea, boolean[] lento, boolean[] cura, int[][] inicios) {
        this.codigos = codigos;
        this.bloquea = bloquea;
        this.lento = lento;
        this.cura = cura;
        this.inicios = inicios;
        this.alcanzable = calcularAlcanzables();
    }

    static Mapa cargar() {
        boolean[] bloquea = new boolean[MAX_CODIGOS];
        boolean[] lento = new boolean[MAX_CODIGOS];
        boolean[] cura = new boolean[MAX_CODIGOS];
        try (Scanner lector = new Scanner(recurso("/escenarios/tiles.txt"), StandardCharsets.UTF_8)) {
            while (lector.hasNextLine()) {
                String linea = lector.nextLine();
                int comentario = linea.indexOf('#');
                if (comentario >= 0) linea = linea.substring(0, comentario);
                linea = linea.trim();
                if (linea.isEmpty()) continue;
                // codigo  imagen  bloquea  lento  cura
                String[] campos = linea.split("\\s+");
                int codigo = Integer.parseInt(campos[0]);
                bloquea[codigo] = campos[2].equals("1");
                lento[codigo] = campos[3].equals("1");
                cura[codigo] = campos[4].equals("1");
            }
        }
        int[][] codigos = new int[FILAS][COLUMNAS];
        try (Scanner lector = new Scanner(recurso("/escenarios/mundo.txt"), StandardCharsets.UTF_8)) {
            for (int[] fila : codigos) {
                for (int col = 0; col < fila.length; col++) fila[col] = lector.nextInt();
            }
        }
        Mapa mapa = new Mapa(codigos, bloquea, lento, cura, INICIOS);
        for (int[] inicio : INICIOS) {
            if (mapa.bloquea(inicio[0], inicio[1])) {
                throw new IllegalStateException("El lugar de salida (fila " + inicio[0]
                        + ", columna " + inicio[1] + ") esta bloqueado en mundo.txt");
            }
        }
        return mapa;
    }

    private static InputStream recurso(String ruta) {
        InputStream recurso = Mapa.class.getResourceAsStream(ruta);
        if (recurso == null) throw new IllegalStateException("Falta " + ruta);
        return recurso;
    }

    int filas() { return codigos.length; }
    int columnas() { return codigos[0].length; }

    boolean dentro(int fila, int col) {
        return fila >= 0 && col >= 0 && fila < codigos.length && col < codigos[0].length;
    }

    // Fuera del mundo cuenta como bloqueado.
    boolean bloquea(int fila, int col) {
        return !dentro(fila, col) || bloquea[codigos[fila][col]];
    }

    boolean lento(int fila, int col) {
        return dentro(fila, col) && lento[codigos[fila][col]];
    }

    boolean cura(int fila, int col) {
        return dentro(fila, col) && cura[codigos[fila][col]];
    }

    // Esquina superior izquierda del cuerpo del jugador `id` al salir, centrado en su tile.
    int inicioX(int id) { return inicios[id][1] * TILE + (TILE - CUERPO) / 2; }
    int inicioY(int id) { return inicios[id][0] * TILE + (TILE - CUERPO) / 2; }

    // true si el tile se alcanza caminando desde los lugares de salida.
    boolean alcanzable(int fila, int col) {
        return dentro(fila, col) && alcanzable[fila][col];
    }

    private boolean[][] calcularAlcanzables() {
        int[][] distancia = new int[filas()][columnas()];
        int[] filasOrigen = new int[inicios.length];
        int[] colsOrigen = new int[inicios.length];
        for (int i = 0; i < inicios.length; i++) {
            filasOrigen[i] = inicios[i][0];
            colsOrigen[i] = inicios[i][1];
        }
        calcularDistancias(filasOrigen, colsOrigen, inicios.length, distancia);
        boolean[][] resultado = new boolean[filas()][columnas()];
        for (int f = 0; f < filas(); f++) {
            for (int c = 0; c < columnas(); c++) resultado[f][c] = distancia[f][c] != LEJOS;
        }
        return resultado;
    }

    // Distancia en tiles (por caminos libres, sin diagonales) desde cada tile hasta el origen mas cercano.
    // LEJOS = no se llega. Un origen sobre un tile bloqueado cuenta, aunque de ahi solo se sale a lo libre.
    void calcularDistancias(int[] filasOrigen, int[] colsOrigen, int cantidad, int[][] salida) {
        int columnas = columnas();
        for (int[] fila : salida) Arrays.fill(fila, LEJOS);
        int[] cola = new int[filas() * columnas];
        int inicio = 0;
        int fin = 0;
        for (int i = 0; i < cantidad; i++) {
            int f = filasOrigen[i];
            int c = colsOrigen[i];
            if (!dentro(f, c) || salida[f][c] == 0) continue;
            salida[f][c] = 0;
            cola[fin++] = f * columnas + c;
        }
        while (inicio < fin) {
            int actual = cola[inicio++];
            int f = actual / columnas;
            int c = actual % columnas;
            for (int[] vecino : VECINOS) {
                int nf = f + vecino[0];
                int nc = c + vecino[1];
                if (!dentro(nf, nc) || bloquea[codigos[nf][nc]] || salida[nf][nc] != LEJOS) continue;
                salida[nf][nc] = salida[f][c] + 1;
                cola[fin++] = nf * columnas + nc;
            }
        }
    }
}
