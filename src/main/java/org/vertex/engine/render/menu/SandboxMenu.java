package org.vertex.engine.render.menu;

import org.vertex.engine.entities.Board;
import org.vertex.engine.enums.GameState;
import org.vertex.engine.enums.Games;
import org.vertex.engine.gui.Colors;
import org.vertex.engine.input.Keyboard;
import org.vertex.engine.interfaces.State;
import org.vertex.engine.interfaces.UI;
import org.vertex.engine.render.Colorblindness;
import org.vertex.engine.render.RenderContext;
import org.vertex.engine.service.BoardService;
import org.vertex.engine.service.GameService;
import org.vertex.engine.service.UIService;

import java.awt.*;

public class SandboxMenu implements UI {
    private static final int ARC = 32;
    private static final int STROKE = 6;

    private final RenderContext render;
    private final BoardService boardService;
    private final UIService uiService;

    public SandboxMenu(RenderContext render,
                       BoardService boardService,
                       UIService uiService) {
        this.render = render;
        this.boardService = boardService;
        this.uiService = uiService;
    }

    @Override
    public void drawMenu(Graphics2D g2) {
        draw(g2);
    }

    @Override
    public boolean canDraw(State state) {
        return state == Games.SANDBOX;
    }

    public void draw(Graphics2D g2) {
        if(GameService.getGame() != Games.SANDBOX) { return; }
        int boardX = render.getBoardRender().getBoardOriginX();
        int boardY = render.getBoardRender().getBoardOriginY();
        int boardWidth = Board.getSquare() * boardService.getBoard().getCol();
        int boardHeight = Board.getSquare() * boardService.getBoard().getRow();
        int boardBottom = boardY + boardHeight;

        g2.setFont(UIService.getFont(UIService.getMENU_FONT()));
        FontMetrics fm = g2.getFontMetrics();
        Keyboard keyboard = boardService.getService().getKeyboard();
        String input = keyboard.getCurrentText();
        int textWidth = fm.stringWidth(input);
        int textHeight = fm.getAscent() + fm.getDescent();

        int innerPadding = render.scale(30);
        int padding = render.scale(90);

        int spacingBelowBoard = render.scale(60);

        int boxWidth = boardWidth;
        int boxHeight = textHeight + 2 * innerPadding;

        int boxX = boardX;
        int boxY = boardBottom + spacingBelowBoard;

        int textX = boxX + (boxWidth - textWidth)/2;
        int textY = boxY + (boxHeight + fm.getAscent() - fm.getDescent())/2;

        UIService.drawBox(g2, STROKE, boxX, boxY, boxWidth,
                boxHeight, ARC, true, false, 255);

        g2.setColor(Colorblindness.filter(Colors.getForeground()));
        g2.drawString(input, textX, textY);
    }
}