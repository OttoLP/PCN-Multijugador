package main;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyManager2 implements KeyListener {
    private volatile boolean keyUp, keyDown, keyLeft, keyRigth, keyAttack;
    private volatile Runnable onToggleMenu;
    private volatile java.util.function.IntConsumer onMenuKey;

    public void setMenuHandlers(Runnable onToggleMenu, java.util.function.IntConsumer onMenuKey) {
        this.onToggleMenu = onToggleMenu;
        this.onMenuKey = onMenuKey;
    }

    @Override
    public void keyTyped(KeyEvent e){

    }

    @Override
    public void keyPressed(KeyEvent e){
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_ESCAPE) {
            Runnable toggle = onToggleMenu;
            if (toggle != null) toggle.run();
            return;
        }
        java.util.function.IntConsumer menuHandler = onMenuKey;
        if (menuHandler != null) {
            menuHandler.accept(code);
        }
        switch(code){
            case KeyEvent.VK_W:
                keyUp = true;
            break;
            case KeyEvent.VK_S:
                keyDown = true;
                break;
            case KeyEvent.VK_A:
                keyLeft = true;
                break;
            case KeyEvent.VK_D:
                keyRigth = true;
                break;
            case KeyEvent.VK_SPACE:
                keyAttack = true;
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e){
        switch(e.getKeyCode()){
            case KeyEvent.VK_W:
                keyUp = false;
                break;
            case KeyEvent.VK_S:
                keyDown = false;
                break;
            case KeyEvent.VK_A:
                keyLeft = false;
                break;
            case KeyEvent.VK_D:
                keyRigth = false;
                break;
            case KeyEvent.VK_SPACE:
                keyAttack = false;
                break;
        }
    }

    public void soltarTeclas() {
        keyUp = keyDown = keyLeft = keyRigth = keyAttack = false;
    }

    public boolean getKeyUp(){
        return this.keyUp;
    }

    public boolean getKeyDown(){
        return this.keyDown;
    }

    public boolean getKeyLeft(){
        return this.keyLeft;
    }

    public boolean getKeyRight(){
        return this.keyRigth;
    }

    public boolean getKeyAttack(){
        return this.keyAttack;
    }
}
