package org.vertex.engine.interfaces;

import java.awt.*;

public interface UI {
    void drawMenu(Graphics2D g2);
    boolean canDraw(State state);
}
