package org.chess.input;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.Map;

public class Keyboard implements KeyListener {

    private final Map<Integer, Boolean> keyStates;
    private final Map<Integer, Boolean> keyProcessed;

    public Keyboard() {
        keyStates = new HashMap<>();
        keyProcessed = new HashMap<>();
    }

    @Override
    public void keyTyped(KeyEvent e) { }

    @Override
    public void keyPressed(KeyEvent e) {
        keyStates.put(e.getKeyCode(), true);
        keyProcessed.putIfAbsent(e.getKeyCode(), false);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        keyStates.put(e.getKeyCode(), false);
        keyProcessed.put(e.getKeyCode(), false);
    }

    private boolean wasKeyPressed(int keyCode) {
        boolean down = keyStates.getOrDefault(keyCode, false);
        boolean processed = keyProcessed.getOrDefault(keyCode, false);

        if (down && !processed) {
            keyProcessed.put(keyCode, true);
            return true;
        }
        return false;
    }

    public boolean wasCancelPressed() { return wasKeyPressed(KeyEvent.VK_C); }
    public boolean wasSelectPressed() { return wasEnterPressed() || wasSpacePressed(); }
    public boolean wasUpPressed() { return wasKeyPressed(KeyEvent.VK_UP); }
    public boolean wasLeftPressed() { return wasKeyPressed(KeyEvent.VK_LEFT); }
    public boolean wasDownPressed() { return wasKeyPressed(KeyEvent.VK_DOWN); }
    public boolean wasRightPressed() { return wasKeyPressed(KeyEvent.VK_RIGHT); }
    public boolean wasEnterPressed() { return wasKeyPressed(KeyEvent.VK_ENTER); }
    public boolean wasSpacePressed() { return wasKeyPressed(KeyEvent.VK_SPACE); }
    public boolean wasControlPressed() { return wasKeyPressed(KeyEvent.VK_CONTROL); }
    public boolean wasZPressed() { return wasKeyPressed(KeyEvent.VK_Z); }
    public boolean wasBPressed() { return wasKeyPressed(KeyEvent.VK_B); }
    public boolean wasRPressed() { return wasKeyPressed(KeyEvent.VK_R); }
    public boolean wasQPressed() { return wasKeyPressed(KeyEvent.VK_Q); }
    public boolean wasOnePressed() { return wasKeyPressed(KeyEvent.VK_1); }
    public boolean wasTwoPressed() { return wasKeyPressed(KeyEvent.VK_2); }
    public boolean wasThreePressed() { return wasKeyPressed(KeyEvent.VK_3); }
    public boolean wasF11Pressed() { return wasKeyPressed(KeyEvent.VK_F11); }

    private boolean isKeyDown(int keyCode) {
        return keyStates.getOrDefault(keyCode, false);
    }

    public boolean isUpDown() { return isKeyDown(KeyEvent.VK_UP); }
    public boolean isLeftDown() { return isKeyDown(KeyEvent.VK_LEFT); }
    public boolean isDownDown() { return isKeyDown(KeyEvent.VK_DOWN); }
    public boolean isRightDown() { return isKeyDown(KeyEvent.VK_RIGHT); }
    public boolean isEnterDown() { return isKeyDown(KeyEvent.VK_ENTER); }
    public boolean isSpaceDown() { return isKeyDown(KeyEvent.VK_SPACE); }
    public boolean isControlDown() { return isKeyDown(KeyEvent.VK_CONTROL); }
    public boolean isZDown() { return isKeyDown(KeyEvent.VK_Z); }
    public boolean isBDown() { return isKeyDown(KeyEvent.VK_B); }
    public boolean isDDown() { return isKeyDown(KeyEvent.VK_D); }
    public boolean isRDown() { return isKeyDown(KeyEvent.VK_R); }
    public boolean isQDown() { return isKeyDown(KeyEvent.VK_Q); }
    public boolean isOneDown() { return isKeyDown(KeyEvent.VK_1); }
    public boolean isTwoDown() { return isKeyDown(KeyEvent.VK_2); }
    public boolean isThreeDown() { return isKeyDown(KeyEvent.VK_3); }

    public boolean isComboPressed(int modifierKey, int triggerKey) {
        return isKeyDown(modifierKey) && wasKeyPressed(triggerKey);
    }
}
