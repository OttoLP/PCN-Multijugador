package main;

import javax.swing.*;
import entidad.Jugador;
import tile.ManejadorTiles;
import red.Cliente;
import red.Mensaje;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class GamePanel extends JPanel implements Runnable {
    final int originalTileSize = 16;
    final int scale = 3;
    final int tileSize = originalTileSize * scale;
    final int maxCols = 26;
    final int maxRows = 15;
    final int widthScreen = tileSize * maxCols;
    final int heightScreen = tileSize * maxRows;
    volatile Thread gameThread;
    KeyManager2 kM = new KeyManager2();
    Jugador[] jugadores = new Jugador[4];
    ManejadorTiles mTi = new ManejadorTiles(this);
    Cliente cliente;
    int FPS = 60;

    public GamePanel(Cliente cliente) {
        this.cliente = cliente;
        int id = cliente.getId();
        jugadores[id] = new Jugador(this, kM, id, cliente.getUsuario());
        jugadores[id].setX(cliente.getX());
        jugadores[id].setY(cliente.getY());
        setPreferredSize(new Dimension(widthScreen, heightScreen));
        setBackground(Color.BLACK);
        setDoubleBuffered(true);
        addKeyListener(kM);
        addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) { kM.soltarTeclas(); }
        });
        setFocusable(true);
    }
    public void startGameThread() {
        gameThread = new Thread(this, "juego");
        gameThread.start();
    }
    @Override
    public void run() {
        while (gameThread != null && cliente.estaConectado()) {
            update();
            repaint();
            try { Thread.sleep(1000 / FPS); }
            catch (InterruptedException e) { break; }
        }
        gameThread = null;
        repaint();
    }
    public void detener() {
        gameThread = null;
        cliente.cerrar();
    }


    public synchronized void update() {
        Mensaje[] estado = cliente.getJugadores();
        for (int i = 0; i < jugadores.length; i++) {
            if (i == cliente.getId()) continue;
            if (estado[i] == null) {
                jugadores[i] = null;
            } else {
                if (jugadores[i] == null || !jugadores[i].getUsuario().equals(estado[i].usuario)) {
                    jugadores[i] = new Jugador(this, null, i, estado[i].usuario);
                    jugadores[i].setX(estado[i].x);
                    jugadores[i].setY(estado[i].y);
                } else {
                    jugadores[i].recibirPosicion(estado[i].x, estado[i].y);
                }
            }
        }
        Jugador local = jugadores[cliente.getId()];
        int antesX = local.getX(), antesY = local.getY();
        local.update();
        if (antesX != local.getX() || antesY != local.getY()) {
            cliente.enviarPosicion(local.getX(), local.getY());
        }
    }
    @Override
    public synchronized void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        mTi.draw(g2);
        for (Jugador jugador : jugadores) {
            if (jugador != null) jugador.draw(g2);
        }
        if (!cliente.estaConectado()) {
            g2.setColor(new Color(0, 0, 0, 190));
            g2.fillRect(0, 0, widthScreen, 50);
            g2.setColor(Color.WHITE);
            g2.drawString(cliente.getError() + " Cierra y vuelve a abrir el cliente.", 20, 30);
        }
        g2.dispose();
    }
    public int getTileSize() { return tileSize; }
    public int getMaxCols() { return maxCols; }
    public int getMaxRows() { return maxRows; }
    public int getWidthScreen() { return widthScreen; }
    public int getHeightScreen() { return heightScreen; }
}
