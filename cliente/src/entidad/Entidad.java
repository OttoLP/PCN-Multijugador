package entidad;

import java.awt.Rectangle;

public class Entidad {
    protected int x, y, velocidad;
    protected int xMundo, yMundo;
    protected String direccion;
    // cuadroActual: cuadro de la animacion de caminata (0 = de pie). cambiaSprite: ciclos de juego por cuadro.
    protected int contadorSprites = 0, cuadroActual = 0, cambiaSprite = 7;
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
