package org.vertex.engine.animations;

import org.vertex.engine.enums.Theme;
import org.vertex.engine.gui.Colors;
import org.vertex.engine.interfaces.Animation;
import org.vertex.engine.render.Colorblindness;
import org.vertex.engine.render.RenderContext;
import org.vertex.engine.service.GUIService;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ToastAnimation implements Animation {
    private final String title;
    private String description;
    private BufferedImage icon;
    private double time = 0;
    private static final double SLIDE_TIME = 0.3;
    private static final double STAY_TIME = 2.0;
    private static final int SLIDE_DISTANCE = 400;
    private static final int WIDTH = 800;
    private static final int HEIGHT = 125;
    private static final int ARC = 25;
    private int baseY;

    public ToastAnimation(String title, String description, int panelHeight, BufferedImage icon) {
        this.title = title;
        this.description = description;
        this.icon = icon;
    }

    public ToastAnimation(String title) {
        this.title = title;
    }

    @Override
    public void update(double delta) {
        time += delta;
    }

    @Override
    public void render(Graphics2D g2) {
        double totalTime = SLIDE_TIME + STAY_TIME + SLIDE_TIME;
        if(time > totalTime) { return; }

        int panelWidth = RenderContext.BASE_WIDTH;
        int x = (panelWidth - WIDTH)/2;
        int y = baseY;

        if(time < SLIDE_TIME) {
            y += (int)((1 - time/SLIDE_TIME) * SLIDE_DISTANCE);
        } else if (time > SLIDE_TIME + STAY_TIME) {
            double t = (time - SLIDE_TIME - STAY_TIME)/SLIDE_TIME;
            y += (int)(t * SLIDE_DISTANCE);
        }

        g2.setColor(Colorblindness.filter(Colors.getForeground()));
        g2.setStroke(new BasicStroke(4));
        g2.drawRoundRect(x, y, WIDTH, HEIGHT, ARC, ARC);
        g2.setColor(Colorblindness.filter(Colors.SETTINGS));
        g2.fillRoundRect(x, y, WIDTH, HEIGHT, ARC, ARC);

        int iconSize = 64;
        if (icon != null) {
            g2.drawImage(icon, x + 20, y + (HEIGHT - iconSize) / 2, iconSize, iconSize, null);
        }

        if(description == null) {
            g2.setFont(GUIService.getFont(GUIService.getMENU_FONT()));
            g2.setColor(Colorblindness.filter(Colors.getHighlight()));
            FontMetrics fm = g2.getFontMetrics();
            int textX = x + 56;
            int textY = y + (HEIGHT + fm.getAscent())/2;
            g2.drawString(title, textX, textY);
            g2.setColor(Colorblindness.filter(Theme.BLACK.getForeground()));
            return;
        }

        g2.setFont(GUIService.getFont(GUIService.getMENU_FONT()));
        g2.setColor(Colorblindness.filter(Colors.getHighlight()));
        FontMetrics fm = g2.getFontMetrics();
        int textX = x + 20 + (icon != null ? iconSize + 32 : 0);
        int textY = y + (HEIGHT + fm.getAscent()) / 2 - 24;
        g2.drawString(title, textX, textY);
        g2.setColor(Colorblindness.filter(Theme.BLACK.getForeground()));
        if(description != null) {
            g2.drawString(description, textX, textY + 40);
        }
    }

    public void setStackIndex(int index, int panelHeight) {
        int spacing = 40;
        this.baseY = panelHeight - 180 - (index * (HEIGHT + spacing));
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