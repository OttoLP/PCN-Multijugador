package entidad;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.awt.Rectangle;

import javax.imageio.ImageIO;

import main.GamePanel;
import main.KeyManager2;

public class Jugador extends Entidad {
    // Color de la camisa de cada jugador (tambien lo usa el minimapa).
    public static final Color[] COLORES = {new Color(237, 28, 36), new Color(40, 110, 235),
            new Color(35, 180, 70), new Color(245, 205, 35)};
    // En un tile lento (pantano) se avanza al 60 %. Igual que Monstruos.PASO_LENTO / PASO en el servidor.
    private static final double FACTOR_LENTO = 0.6;
    // Hoja del mago (spritesjugador/mago.png): cuadros de 16 x 16, 4 por fila (animacion de caminata, el
    // primero es de pie). Cada fila es una direccion, en este orden: abajo, derecha, izquierda, arriba.
    // Las filas 5 y 6 de la hoja todavia no se usan.
    private static final int TAM_CUADRO = 16;
    private static final int CUADROS = 4;
    private static final String[] DIRECCIONES = {"abajo", "derecha", "izquierda", "arriba"};
    // Solo cambia el azul del sombrero y la tunica (claro, medio y oscuro); lo demas queda como en la hoja.
    private static final int[] AZULES = {0xFF284884, 0xFF273E6F, 0xFF19264E};
    // Sombrero y tunica de cada jugador: rojo, azul, verde y amarillo (claro, medio y oscuro).
    private static final int[][] TUNICAS = {
            {0xFFAC343C, 0xFF903339, 0xFF652125},   // rojo
            {0xFF284884, 0xFF273E6F, 0xFF19264E},   // azul, el original de la hoja
            {0xFF2F8B46, 0xFF2D753F, 0xFF1D522A},   // verde
            {0xFFCDB053, 0xFFAC964D, 0xFF796933},   // amarillo
    };
    // Animacion de los otros jugadores: tiempo entre cuadros y cuanto sin moverse para pasar a "de pie".
    private static final long MS_POR_CUADRO = 115;
    private static final long MS_PARA_REPOSO = 200;
    private GamePanel gp;
    private KeyManager2 mT;
    private int id;
    private String usuario;
    private final int xPantalla, yPantalla;
    // La vida la decide el servidor; aqui solo se guarda para dibujarla.
    private int vida = 100, vidaMax = 100;
    // Mejoras que decide el servidor. La velocidad tiene decimales y se reparte en pasos enteros.
    private int nivelDanio, nivelVelocidad;
    private double velocidadReal = 4, restoVelocidad;
    private final BufferedImage[][] cuadros = new BufferedImage[DIRECCIONES.length][CUADROS];
    private boolean remoto;            // los otros jugadores se animan con lo que reciben del servidor
    private long ultimoMovimiento, ultimoCuadro;

    public Jugador(GamePanel gp, KeyManager2 mT, int id, String usuario){
        this.gp = gp;
        this.mT = mT;
        this.id = id;
        this.usuario = usuario;
        this.xPantalla = gp.getWidthScreen() / 2 - (gp.getTileSize() / 2);
        this.yPantalla = gp.getHeightScreen() / 2 - (gp.getTileSize() / 2);
        this.hitbox = new Rectangle(8, 16, 32, 32);
        configuracionInicial();
        getSpritesJugador();
    }

    public void configuracionInicial(){
        this.x = gp.getWidthScreen() / 2;
        this.y = gp.getHeightScreen() / 2;
        this.velocidad = 4;
        this.setDireccion("abajo");
    }

    public void getSpritesJugador(){
        try (InputStream recurso = getClass().getResourceAsStream("/spritesjugador/mago.png")) {
            if (recurso == null) throw new IOException("Falta spritesjugador/mago.png");
            BufferedImage hoja = colorear(ImageIO.read(recurso));
            for (int fila = 0; fila < DIRECCIONES.length; fila++) {
                for (int col = 0; col < CUADROS; col++) {
                    cuadros[fila][col] = hoja.getSubimage(col * TAM_CUADRO, fila * TAM_CUADRO, TAM_CUADRO, TAM_CUADRO);
                }
            }
        }catch(IOException e){
            throw new IllegalStateException("No se pudieron cargar los sprites", e);
        }
    }

    // Copia la hoja cambiando solo los tres azules del sombrero y la tunica por los de este jugador.
    private BufferedImage colorear(BufferedImage hoja) {
        BufferedImage copia = new BufferedImage(hoja.getWidth(), hoja.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int fila = 0; fila < hoja.getHeight(); fila++) {
            for (int col = 0; col < hoja.getWidth(); col++) {
                int pixel = hoja.getRGB(col, fila);
                for (int k = 0; k < AZULES.length; k++) {
                    if (pixel == AZULES[k]) {
                        pixel = TUNICAS[id][k];
                        break;
                    }
                }
                copia.setRGB(col, fila, pixel);
            }
        }
        return copia;
    }

    private int filaDeDireccion() {
        for (int i = 0; i < DIRECCIONES.length; i++) {
            if (DIRECCIONES[i].equals(direccion)) return i;
        }
        return 0;
    }

    // Los otros jugadores deducen direccion y animacion a partir del desplazamiento.
    public void recibirPosicion(int nuevoX, int nuevoY) {
        if (nuevoY < y) setDireccion("arriba");
        else if (nuevoY > y) setDireccion("abajo");
        else if (nuevoX < x) setDireccion("izquierda");
        else if (nuevoX > x) setDireccion("derecha");
        if (nuevoX != x || nuevoY != y) {
            long ahora = System.currentTimeMillis();
            remoto = true;
            ultimoMovimiento = ahora;
            if (ahora - ultimoCuadro >= MS_POR_CUADRO) {
                cuadroActual = (cuadroActual + 1) % CUADROS;
                ultimoCuadro = ahora;
            }
        }
        x = nuevoX;
        y = nuevoY;
    }

    public void update(){
        boolean moviendo = false;

        if (mT.getKeyUp()) {
            setDireccion("arriba"); moviendo = true;
        } else if (mT.getKeyDown()) {
            setDireccion("abajo"); moviendo = true;
        } else if (mT.getKeyLeft()) {
            setDireccion("izquierda"); moviendo = true;
        } else if (mT.getKeyRight()) {
            setDireccion("derecha"); moviendo = true;
        }

        if (moviendo) {
            // Con decimales (por ejemplo 4.3) se avanzan 4 px casi todos los cuadros y 5 de vez en cuando.
            restoVelocidad += enTileLento() ? velocidadReal * FACTOR_LENTO : velocidadReal;
            this.velocidad = (int) restoVelocidad;
            restoVelocidad -= this.velocidad;
            this.colisionActivada = false;
            gp.getDetectorColisiones().revisaTile(this);

            if (!this.colisionActivada) {
                switch (this.direccion) {
                    case "arriba":    setY(getY() - getVelocidad()); break;
                    case "abajo":     setY(getY() + getVelocidad()); break;
                    case "izquierda": setX(getX() - getVelocidad()); break;
                    case "derecha":   setX(getX() + getVelocidad()); break;
                }
            }
            contadorSprites++;
        } else {
            // Quieto: primer cuadro de la hoja (de pie).
            contadorSprites = 0;
            cuadroActual = 0;
        }

        x = Math.max(0, Math.min(x, gp.getWidthWorld() - gp.getTileSize()));
        y = Math.max(0, Math.min(y, gp.getHeightWorld() - gp.getTileSize()));

        if (contadorSprites > cambiaSprite) {
            cuadroActual = (cuadroActual + 1) % CUADROS;
            contadorSprites = 0;
        }
    }

    // true si el centro del jugador esta sobre un tile lento (pantano).
    private boolean enTileLento() {
        int size = gp.getTileSize();
        var tiles = gp.getManejadorTiles();
        return tiles.getTile(tiles.getCodigoMapaTiles((y + 32) / size, (x + 24) / size)).getLento();
    }

    public void draw(Graphics2D g2, int camaraX, int camaraY){
        // Un jugador remoto que dejo de moverse se muestra de pie.
        int cuadro = (remoto && System.currentTimeMillis() - ultimoMovimiento > MS_PARA_REPOSO) ? 0 : cuadroActual;
        BufferedImage sprite = cuadros[filaDeDireccion()][cuadro];
        int pantallaX = getX() - camaraX;
        int pantallaY = getY() - camaraY;
        // Un jugador caido se ve casi transparente y sin barra de vida.
        Composite normal = g2.getComposite();
        if (isMuerto()) g2.setComposite(AlphaComposite.SrcOver.derive(0.3f));
        g2.drawImage(sprite, pantallaX, pantallaY, gp.getTileSize(), gp.getTileSize(), null);
        g2.setComposite(normal);
        if (!isMuerto()) dibujaBarraVida(g2, pantallaX, pantallaY);
        
        /* 
        g2.setColor(Color.BLACK);
        g2.drawString(usuario, x + 1, Math.max(13, y - 3) + 1);
        g2.setColor(Color.WHITE);
        g2.drawString(usuario, x, Math.max(13, y - 3));
        */
    }

    private void dibujaBarraVida(Graphics2D g2, int pantallaX, int pantallaY) {
        int ancho = 40, alto = 5;
        int barraX = pantallaX + (gp.getTileSize() - ancho) / 2;
        int barraY = pantallaY - 8;
        g2.setColor(new Color(20, 20, 20, 200));
        g2.fillRect(barraX - 1, barraY - 1, ancho + 2, alto + 2);
        g2.setColor(new Color(190, 40, 40));
        g2.fillRect(barraX, barraY, ancho * vida / Math.max(1, vidaMax), alto);
    }

    // Setters
    public void setVida(int vida, int vidaMax) {
        this.vida = vida;
        this.vidaMax = vidaMax;
    }
    public void setMejoras(int nivelDanio, int nivelVelocidad, double velocidad) {
        this.nivelDanio = nivelDanio;
        this.nivelVelocidad = nivelVelocidad;
        this.velocidadReal = velocidad;
    }
    public void setX(int valor){
        this.x = valor;
    }
    public void setY(int valor){ this.y = valor; }

    // Getters
    public int getX(){
        return this.x;
    }
    public int getY(){ return this.y; }
    public String getUsuario() { return usuario; }
    public int getVida() { return vida; }
    public int getVidaMax() { return vidaMax; }
    public int getNivelDanio() { return nivelDanio; }
    public int getNivelVelocidad() { return nivelVelocidad; }
    public boolean isMuerto() { return vida <= 0; }
    public int getVelocidad(){
        return this.velocidad;
    }
    public int getPantallaX(){ return this.xPantalla; }
    public int getPantallaY(){ return this.yPantalla; }
    public int getAreaSolidaAncho(){ return this.hitbox.width; }
    public int getAreaSolidaAlto(){ return this.hitbox.height; }
    public int getMundoX(){ return this.x; }
    public int getMundoY(){ return this.y; }
    public int getAreaSolidaX(){ return this.hitbox.x; }
    public int getAreaSolidaY(){ return this.hitbox.y; }
}
