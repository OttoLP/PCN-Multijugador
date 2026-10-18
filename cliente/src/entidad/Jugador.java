package entidad;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import main.GamePanel;
import main.KeyManager2;

public class Jugador extends Entidad {
    private GamePanel gp;
    private KeyManager2 mT;
    private int id;
    private String usuario;

    public Jugador(GamePanel gp, KeyManager2 mT, int id, String usuario){
        this.gp = gp;
        this.mT = mT;
        this.id = id;
        this.usuario = usuario;

        configuracionInicial();
        getSpritesJugador();
    }
    public void configuracionInicial(){
        this.x = 100;
        this.y = 100;
        this.velocidad = 4;
        this.setDireccion("abajo");
    }

    public void getSpritesJugador(){
        try{
            this.arriba1 = colorear(ImageIO.read(getClass().getResourceAsStream("/spritesjugador/moverArriba1.png")));
            this.arriba2 = colorear(ImageIO.read(getClass().getResourceAsStream("/spritesjugador/moverArriba2.png")));
            this.abajo1 = colorear(ImageIO.read(getClass().getResourceAsStream("/spritesjugador/moverAbajo1.png")));
            this.abajo2 = colorear(ImageIO.read(getClass().getResourceAsStream("/spritesjugador/moverAbajo2.png")));
            this.izquierda1 = colorear(ImageIO.read(getClass().getResourceAsStream("/spritesjugador/moverIzquierda1.png")));
            this.izquierda2 = colorear(ImageIO.read(getClass().getResourceAsStream("/spritesjugador/moverIzquierda2.png")));
            this.derecha1 = colorear(ImageIO.read(getClass().getResourceAsStream("/spritesjugador/moverDerecha1.png")));
            this.derecha2 = colorear(ImageIO.read(getClass().getResourceAsStream("/spritesjugador/moverDerecha2.png")));
        }catch(IOException e){
            throw new IllegalStateException("No se pudieron cargar los sprites", e);
        }
    }


    private BufferedImage colorear(BufferedImage imagen) {

        /* 
        Color[] colores = {new Color(237, 28, 36), new Color(40, 110, 235),
                new Color(35, 180, 70), new Color(245, 205, 35)};
        Color color = colores[id];
        for (int fila = 7; fila <= 12; fila++) {
            for (int col = 0; col < imagen.getWidth(); col++) {
                int pixel = imagen.getRGB(col, fila);
                boolean claro = pixel == 0xFFED1C24;
                boolean oscuro = pixel == 0xFF880015;
                // En las vistas de espalda, fila 7 aun tiene pelo en el centro.
                if (fila == 7 && col >= 4 && col <= 11) continue;
                if (claro || oscuro) {
                    imagen.setRGB(col, fila, (oscuro ? color.darker() : color).getRGB());
                }
            }
        }*/
        return imagen;
    }

    // Los otros jugadores deducen direccion y animacion a partir del desplazamiento.
    public void recibirPosicion(int nuevoX, int nuevoY) {
        if (nuevoY < y) setDireccion("arriba");
        else if (nuevoY > y) setDireccion("abajo");
        else if (nuevoX < x) setDireccion("izquierda");
        else if (nuevoX > x) setDireccion("derecha");
        if (nuevoX != x || nuevoY != y) {
            contadorSprites++;
            if (contadorSprites > cambiaSprite) {
                numeroSprites = numeroSprites == 1 ? 2 : 1;
                contadorSprites = 0;
            }
        }
        x = nuevoX;
        y = nuevoY;
    }

    public String getUsuario() { return usuario; }

    public void update(){
        if(mT.getKeyUp() || mT.getKeyDown() || mT.getKeyLeft() || mT.getKeyRight()){
            this.contadorSprites++;
        }
        if (mT.getKeyUp()) {
            this.setY(this.getY() - this.getVelocidad());
            this.setDireccion("arriba");
        } else if (mT.getKeyDown()) {
            this.setY(this.getY() + this.getVelocidad());
            this.setDireccion("abajo");                
        } else if (mT.getKeyLeft()) {
            this.setX(this.getX() - this.getVelocidad());
            this.setDireccion   ("izquierda");
        } else if (mT.getKeyRight()) {
            this.setX(this.getX() + this.getVelocidad());
            this.setDireccion("derecha");
        }
        
        // Mantener el sprite dentro de la ventana de 26 x 15 tiles.
        x = Math.max(0, Math.min(x, gp.getWidthScreen() - gp.getTileSize()));
        y = Math.max(0, Math.min(y, gp.getHeightScreen() - gp.getTileSize()));
        if(this.contadorSprites > this.cambiaSprite){
            if(this.numeroSprites == 1)
                this.numeroSprites = 2;
            else
                this.numeroSprites = 1;
            this.contadorSprites = 0;
        }
    }

    public void draw(Graphics2D g2){
        BufferedImage sprite = null;
        switch (this.getDireccion()) {
            case "arriba":
                if(this.numeroSprites == 1)
                    sprite = this.arriba1;
                if (this.numeroSprites == 2)
                    sprite = this.arriba2;
            break;
            case "abajo":
                if(this.numeroSprites == 1)
                    sprite = this.abajo1;
                if (this.numeroSprites == 2)
                    sprite = this.abajo2;
            break;
            case "izquierda":
                if(this.numeroSprites == 1)
                    sprite = this.izquierda1;
                if (this.numeroSprites == 2)
                    sprite = this.izquierda2;
            break;
            case "derecha":
                if(this.numeroSprites == 1)
                    sprite = this.derecha1;
                if (this.numeroSprites == 2)
                    sprite = this.derecha2;
            break;
        }
        g2.drawImage(sprite, this.getX(), this.getY(), gp.getTileSize(), gp.getTileSize(), null);
        
        /* 
        g2.setColor(Color.BLACK);
        g2.drawString(usuario, x + 1, Math.max(13, y - 3) + 1);
        g2.setColor(Color.WHITE);
        g2.drawString(usuario, x, Math.max(13, y - 3));
        */
    }
    public int getX(){
        return this.x;
    }
    public int getY(){
        return this.y;
    }
    public int getVelocidad(){
        return this.velocidad;
    }
    public void setX(int valor){
        this.x = valor;
    }
    public void setY(int valor){
        this.y = valor;
    }
}
