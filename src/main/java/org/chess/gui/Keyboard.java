package org.chess.gui;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class Keyboard implements KeyListener {
    private boolean bPressedEvent = false;

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if(key == KeyEvent.VK_B) {
            bPressedEvent = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    public boolean wasBPressed() {
        if(bPressedEvent) {
            bPressedEvent = false;
            return true;
        }
        return false;
    }
}
