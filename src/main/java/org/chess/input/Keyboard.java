package org.chess.input;

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

    public boolean wasUpPressed() { return wasKeyPressed(KeyEvent.VK_UP); }
    public boolean wasLeftPressed() { return wasKeyPressed(KeyEvent.VK_LEFT); }
    public boolean wasDownPressed() { return wasKeyPressed(KeyEvent.VK_DOWN); }
    public boolean wasRightPressed() { return wasKeyPressed(KeyEvent.VK_RIGHT); }
    public boolean wasEnterPressed() { return wasKeyPressed(KeyEvent.VK_ENTER); }
    public boolean wasSpacePressed() { return wasKeyPressed(KeyEvent.VK_SPACE); }
    public boolean wasZPressed() { return wasKeyPressed(KeyEvent.VK_Z); }
    public boolean wasBPressed() { return wasKeyPressed(KeyEvent.VK_B); }

    public boolean wasSelectPressed() {
        return wasEnterPressed() || wasSpacePressed();
    }
}
