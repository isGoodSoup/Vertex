package org.vertex.engine.animations;

import org.vertex.engine.interfaces.Animation;
import org.vertex.engine.render.RenderContext;
import org.vertex.engine.service.GUIService;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ToastAnimation implements Animation {
    private final String title;
    private final String description;
    private final BufferedImage icon;
    private double time = 0;
    private static final double SLIDE_TIME = 0.5;
    private static final double STAY_TIME = 2.0;
    private static final int SLIDE_DISTANCE = 210;
    private static final int WIDTH = 800;
    private static final int HEIGHT = 125;
    private static final int ARC = 25;
    private final int baseY;

    public ToastAnimation(String title, String description, int panelHeight, BufferedImage icon) {
        this.title = title;
        this.description = description;
        this.icon = icon;
        this.baseY = panelHeight - 160;
    }

    @Override
    public void update(double delta) {
        time += delta;
    }

    @Override
    public void render(Graphics2D g2) {
        double totalTime = SLIDE_TIME + STAY_TIME + SLIDE_TIME;
        if(time > totalTime) return;

        int panelWidth = RenderContext.BASE_WIDTH;
        int x = (panelWidth - WIDTH) / 2;
        int y = baseY;

        if(time < SLIDE_TIME) { // sliding in
            y += (int)((1 - time / SLIDE_TIME) * SLIDE_DISTANCE);
        } else if (time > SLIDE_TIME + STAY_TIME) { // sliding out
            double t = (time - SLIDE_TIME - STAY_TIME) / SLIDE_TIME;
            y += (int)(t * SLIDE_DISTANCE);
        }

        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(8));
        g2.drawRoundRect(x, y, WIDTH, HEIGHT, ARC, ARC);
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRoundRect(x, y, WIDTH, HEIGHT, ARC, ARC);

        int iconSize = 64;
        if (icon != null) {
            g2.drawImage(icon, x + 20, y + (HEIGHT - iconSize) / 2, iconSize, iconSize, null);
        }

        g2.setFont(GUIService.getFont(GUIService.getMENU_FONT()));
        g2.setColor(Color.YELLOW);
        FontMetrics fm = g2.getFontMetrics();
        int textX = x + 20 + (icon != null ? iconSize + 32 : 0);
        int textY = y + (HEIGHT + fm.getAscent()) / 2 - 24;
        g2.drawString(title, textX, textY);
        g2.setColor(Color.WHITE);
        g2.drawString(description, textX, textY + 40);
    }

    @Override
    public boolean isFinished() {
        return time >= (SLIDE_TIME + STAY_TIME + SLIDE_TIME);
    }

    @Override
    public boolean affects(Object obj) {
        return false;
    }
}
