package entidad;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

public class Entidad {
    protected int x, y, velocidad;
    protected int xMundo, yMundo;
    protected BufferedImage arriba1,arriba2,abajo1,abajo2,izquierda1,izquierda2,derecha1,derecha2;
    protected String direccion;
    protected int contadorSprites = 0, numeroSprites = 1, cambiaSprite = 10;
    protected Rectangle hitbox;
    protected boolean colisionActivada = false;

    // Getters
    public String getDireccion() {
        return this.direccion;
    }

    // Setters
    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }
    public void setColisionActivada(boolean valor){ this.colisionActivada = valor; }
}
