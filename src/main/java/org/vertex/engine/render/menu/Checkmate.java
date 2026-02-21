package org.vertex.engine.render.menu;

import org.vertex.engine.enums.GameState;
import org.vertex.engine.gui.Colors;
import org.vertex.engine.interfaces.State;
import org.vertex.engine.interfaces.UI;
import org.vertex.engine.render.Colorblindness;
import org.vertex.engine.render.RenderContext;
import org.vertex.engine.service.GameService;
import org.vertex.engine.service.UIService;

import java.awt.*;

public class Checkmate implements UI {
    private final UIService uiService;
    private final GameService gameService;
    private final RenderContext render;
    private final int totalWidth;

    public Checkmate(UIService uiService, GameService gameService,
                     RenderContext render, int totalWidth) {
        this.uiService = uiService;
        this.gameService = gameService;
        this.render = render;
        this.totalWidth = totalWidth;
    }

    private int getCenterX(int containerWidth, int contentWidth) {
        return (containerWidth - contentWidth)/2;
    }

    @Override
    public void drawMenu(Graphics2D g2) {
        draw(g2);
    }

    @Override
    public boolean canDraw(State state) {
        return state == GameState.CHECKMATE;
    }

    public void draw(Graphics2D g2) {
        if(gameService.getState() != GameState.CHECKMATE) { return; }

        g2.setFont(UIService.getFontBold(UIService.getMENU_FONT()));
        FontMetrics fm = g2.getFontMetrics();

        int headerY = render.getOffsetY() + render.scale(200);
        int headerWidth = fm.stringWidth("CHECKMATE");

        g2.setColor(Colorblindness.filter(Colors.getForeground()));
        String text = gameService.getState() == GameState.CHECKMATE ?
                "CHECKMATE" : "STALEMATE";
        g2.drawString(text, getCenterX(totalWidth, headerWidth), headerY);
    }
}