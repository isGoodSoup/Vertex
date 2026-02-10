package org.chess.gui;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.Map;

public class Keyboard implements KeyListener {
    private final Map<Integer, Boolean> keyStates;

    public Keyboard() {
        this.keyStates = new HashMap<>();
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        keyStates.put(e.getKeyCode(), true);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        keyStates.put(e.getKeyCode(), false);
    }

    public boolean wasKeyPressed(int keyCode) {
        if (keyStates.getOrDefault(keyCode, false)) {
            keyStates.put(keyCode, false);
            return true;
        }
        return false;
    }

    public boolean wasWPressed() { return wasKeyPressed(KeyEvent.VK_W); }
    public boolean wasAPressed() { return wasKeyPressed(KeyEvent.VK_A); }
    public boolean wasSPressed() { return wasKeyPressed(KeyEvent.VK_S); }
    public boolean wasDPressed() { return wasKeyPressed(KeyEvent.VK_D); }
    public boolean wasEnterPressed() { return wasKeyPressed(KeyEvent.VK_ENTER); }
    public boolean wasZPressed() { return wasKeyPressed(KeyEvent.VK_Z); }
    public boolean wasBPressed() { return wasKeyPressed(KeyEvent.VK_B); }

    public boolean wasSelectPressed() {
        return wasEnterPressed() || wasZPressed();
    }
}
