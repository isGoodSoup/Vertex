package org.vertex.engine.input;

import org.vertex.engine.service.BooleanService;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.Map;

public class Keyboard implements KeyListener {
    private final Map<Integer, Boolean> keyStates;
    private final Map<Integer, Boolean> keyProcessed;
    private StringBuilder textBuffer = new StringBuilder();

    public Keyboard() {
        keyStates = new HashMap<>();
        keyProcessed = new HashMap<>();
    }

    public String consumeText() {
        String text = textBuffer.toString();
        textBuffer.setLength(0);
        return text;
    }

    public String getCurrentText() {
        return textBuffer.toString();
    }

    @Override
    public void keyTyped(KeyEvent e) {
        if(!BooleanService.canType) { return; }
        char c = e.getKeyChar();
        if(!Character.isISOControl(c)) {
            textBuffer.append(c);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        keyStates.put(e.getKeyCode(), true);
        keyProcessed.putIfAbsent(e.getKeyCode(), false);
        if(BooleanService.canType) {
            if(e.getKeyCode() == KeyEvent.VK_BACK_SPACE && !textBuffer.isEmpty()) {
                textBuffer.deleteCharAt(textBuffer.length() - 1);
            }
        }
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
    public boolean wasTabPressed() { return wasKeyPressed(KeyEvent.VK_TAB); }
    public boolean wasUpPressed() { return wasKeyPressed(KeyEvent.VK_UP); }
    public boolean wasLeftPressed() { return wasKeyPressed(KeyEvent.VK_LEFT); }
    public boolean wasDownPressed() { return wasKeyPressed(KeyEvent.VK_DOWN); }
    public boolean wasRightPressed() { return wasKeyPressed(KeyEvent.VK_RIGHT); }
    public boolean wasEnterPressed() { return wasKeyPressed(KeyEvent.VK_ENTER); }
    public boolean wasSpacePressed() { return wasKeyPressed(KeyEvent.VK_SPACE); }
    public boolean wasZPressed() { return wasKeyPressed(KeyEvent.VK_Z); }
    public boolean wasRPressed() { return wasKeyPressed(KeyEvent.VK_R); }
    public boolean wasQPressed() { return wasKeyPressed(KeyEvent.VK_Q); }
    public boolean wasOnePressed() { return wasKeyPressed(KeyEvent.VK_1); }
    public boolean wasTwoPressed() { return wasKeyPressed(KeyEvent.VK_2); }
    public boolean wasThreePressed() { return wasKeyPressed(KeyEvent.VK_3); }
    public boolean wasF11Pressed() { return wasKeyPressed(KeyEvent.VK_F11); }

    private boolean isKeyDown(int keyCode) {
        return keyStates.getOrDefault(keyCode, false);
    }

    public boolean isEscapeDown() { return isKeyDown(KeyEvent.VK_ESCAPE); }

    public boolean isComboPressed(int modifierKey, int triggerKey) {
        return isKeyDown(modifierKey) && wasKeyPressed(triggerKey);
    }
}
