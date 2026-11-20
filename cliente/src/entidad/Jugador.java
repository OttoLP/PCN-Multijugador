package entidad;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.awt.Rectangle;

import javax.imageio.ImageIO;

import main.GamePanel;
import main.KeyManager2;
import main.Sonido;

public class Jugador extends Entidad {
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

    // Hoja de sprites 48x48 (idle: 0-2, mover: 3-5, atacar: 6-8, morir: 9)
    private static final int FRAME_SIZE = 48;
    private BufferedImage[] idleAbajo, idleDerecha, idleIzquierda, idleArriba;
    private BufferedImage[] moverAbajo, moverDerecha, moverIzquierda, moverArriba;
    private BufferedImage[] atacarAbajo, atacarDerecha, atacarIzquierda, atacarArriba;
    private BufferedImage[] morir;
    private boolean moviendo;
    private int frameIdx = 0;
    private int frameTick = 0;
    private int deathFrame = 0;
    private long atacarHasta = 0;
    private int ataqueFrame = 0;

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

    private static final Color[] COLORES_JUGADOR = {
            new Color(237, 28, 36),   // 0: Rojo
            new Color(40, 110, 235),  // 1: Azul
            new Color(35, 180, 70),   // 2: Verde
            new Color(245, 205, 35)   // 3: Amarillo
    };

    public void getSpritesJugador(){
        try{
            BufferedImage original = ImageIO.read(getClass().getResourceAsStream("/spritesjugador/player_sheet.png"));
            if (original == null) throw new IOException("Falta /spritesjugador/player_sheet.png");
            BufferedImage hoja = colorearHoja(original);

            idleAbajo = cortarFila(hoja, 0, 6);
            idleDerecha = cortarFila(hoja, 1, 6);
            idleIzquierda = voltearFila(idleDerecha);
            idleArriba = cortarFila(hoja, 2, 6);

            moverAbajo = cortarFila(hoja, 3, 6);
            moverDerecha = cortarFila(hoja, 4, 6);
            moverIzquierda = voltearFila(moverDerecha);
            moverArriba = cortarFila(hoja, 5, 6);

            atacarAbajo = cortarFila(hoja, 6, 4);
            atacarDerecha = cortarFila(hoja, 7, 4);
            atacarIzquierda = voltearFila(atacarDerecha);
            atacarArriba = cortarFila(hoja, 8, 4);

            morir = cortarFila(hoja, 9, 3);
        }catch(IOException e){
            throw new IllegalStateException("No se pudieron cargar los sprites", e);
        }
    }

    private BufferedImage colorearHoja(BufferedImage original) {
        BufferedImage copia = new BufferedImage(
                original.getWidth(), original.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Color color = COLORES_JUGADOR[Math.floorMod(id, COLORES_JUGADOR.length)];
        int claro = color.getRGB();
        int oscuro = color.darker().getRGB();
        for (int y = 0; y < original.getHeight(); y++) {
            for (int x = 0; x < original.getWidth(); x++) {
                int pixel = original.getRGB(x, y);
                if (pixel == 0xFF2C65B5) {
                    copia.setRGB(x, y, claro);
                } else if (pixel == 0xFF1D438A) {
                    copia.setRGB(x, y, oscuro);
                } else {
                    copia.setRGB(x, y, pixel);
                }
            }
        }
        return copia;
    }

    private static BufferedImage[] cortarFila(BufferedImage hoja, int fila, int columnas) {
        BufferedImage[] frames = new BufferedImage[columnas];
        for (int col = 0; col < columnas; col++) {
            frames[col] = hoja.getSubimage(col * FRAME_SIZE, fila * FRAME_SIZE, FRAME_SIZE, FRAME_SIZE);
        }
        return frames;
    }

    private static BufferedImage[] voltearFila(BufferedImage[] origen) {
        BufferedImage[] volteados = new BufferedImage[origen.length];
        for (int i = 0; i < origen.length; i++) {
            BufferedImage src = origen[i];
            BufferedImage dst = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = dst.createGraphics();
            g.drawImage(src, src.getWidth(), 0, -src.getWidth(), src.getHeight(), null);
            g.dispose();
            volteados[i] = dst;
        }
        return volteados;
    }

    public void iniciarAtaque() {
        if (!estaAtacando()) {
            Sonido.reproducir("/sonidos/ataque.wav");
        }
        atacarHasta = System.currentTimeMillis() + 240;
        ataqueFrame = 0;
    }

    public boolean estaAtacando() {
        return System.currentTimeMillis() < atacarHasta;
    }

    // Los otros jugadores deducen direccion y animacion a partir del desplazamiento.
    public void recibirPosicion(int nuevoX, int nuevoY) {
        if (nuevoY < y) setDireccion("arriba");
        else if (nuevoY > y) setDireccion("abajo");
        else if (nuevoX < x) setDireccion("izquierda");
        else if (nuevoX > x) setDireccion("derecha");
        moviendo = (nuevoX != x || nuevoY != y);
        avanzarAnimacion();
        x = nuevoX;
        y = nuevoY;
    }

    public void update(){
        moviendo = false;

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
            restoVelocidad += velocidadReal;
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
        }

        x = Math.max(0, Math.min(x, gp.getWidthWorld() - gp.getTileSize()));
        y = Math.max(0, Math.min(y, gp.getHeightWorld() - gp.getTileSize()));

        avanzarAnimacion();
    }

    private void avanzarAnimacion() {
        frameTick++;
        int intervalo = moviendo ? 6 : 8;
        if (frameTick >= intervalo) {
            frameTick = 0;
            frameIdx = (frameIdx + 1) % 6;
            if (isMuerto() && deathFrame < morir.length - 1) {
                deathFrame++;
            } else if (!isMuerto()) {
                deathFrame = 0;
            }
        }
        if (System.currentTimeMillis() < atacarHasta && frameTick % 4 == 0) {
            ataqueFrame = Math.min(ataqueFrame + 1, 3);
        }
    }

    public void draw(Graphics2D g2, int camaraX, int camaraY){
        BufferedImage sprite;
        if (isMuerto()) {
            sprite = morir[deathFrame];
        } else {
            boolean atacando = System.currentTimeMillis() < atacarHasta;
            BufferedImage[] anim;
            if (atacando) {
                anim = switch (this.getDireccion()) {
                    case "arriba" -> atacarArriba;
                    case "izquierda" -> atacarIzquierda;
                    case "derecha" -> atacarDerecha;
                    default -> atacarAbajo;
                };
                sprite = anim[ataqueFrame % anim.length];
            } else if (moviendo) {
                anim = switch (this.getDireccion()) {
                    case "arriba" -> moverArriba;
                    case "izquierda" -> moverIzquierda;
                    case "derecha" -> moverDerecha;
                    default -> moverAbajo;
                };
                sprite = anim[frameIdx % anim.length];
            } else {
                anim = switch (this.getDireccion()) {
                    case "arriba" -> idleArriba;
                    case "izquierda" -> idleIzquierda;
                    case "derecha" -> idleDerecha;
                    default -> idleAbajo;
                };
                sprite = anim[frameIdx % anim.length];
            }
        }
        int pantallaX = getX() - camaraX;
        int pantallaY = getY() - camaraY;
        int tile = gp.getTileSize();
        int drawSize = tile * 2;
        int drawX = pantallaX - (drawSize - tile) / 2;
        int drawY = pantallaY - (drawSize - tile) * 3 / 4;
        g2.drawImage(sprite, drawX, drawY, drawSize, drawSize, null);
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
