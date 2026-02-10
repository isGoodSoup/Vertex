package org.chess.render;

import org.chess.entities.Board;
import org.chess.gui.Mouse;
import org.chess.service.BooleanService;
import org.chess.service.GUIService;
import org.chess.service.GameService;

import java.awt.*;

public class MenuRender {

    public static final String[] optionsMenu = { "PLAY AGAINST", "EXIT" };
    public static final String[] optionsMode = { "PLAYER", "AI", "CHAOS" };
    private int lastHoveredIndex = -1;

    private final GUIService guiService;
    private final Mouse mouse;
    private final GameService gameService;

    public MenuRender(GUIService guiService, GameService gameService, Mouse mouse) {
        this.guiService = guiService;
        this.gameService = gameService;
        this.mouse = mouse;
    }

    public static void drawRandomBackground(boolean isColor) {
        GUIService.setNewBackground(isColor ? Board.getEven() : Board.getOdd());
        GUIService.setNewForeground(isColor ? Board.getOdd() : Board.getEven());
    }

    private static void drawLogo(Graphics2D g2) {
        if(GUIService.getLogo() == null) return;

        int boardWidth = Board.getSquare() * 8;
        int boardCenterX = GUIService.getBOARD_OFFSET_X() + boardWidth/2;
        int logoWidth = GUIService.getLogo().getWidth()/3;
        int logoHeight = GUIService.getLogo().getHeight()/3;
        int x = boardCenterX - logoWidth/2;
        int y = GUIService.getHEIGHT()/7;
        g2.drawImage(GUIService.getLogo(), x, y, logoWidth, logoHeight, null);
    }

    public void drawGraphics(Graphics2D g2, String[] options) {
        g2.setColor(GUIService.getNewBackground());
        g2.fillRect(0, 0, GUIService.getWIDTH(), GUIService.getHEIGHT());
        g2.setFont(GUIService.getFont(GUIService.getMENU_FONT()));
        g2.setColor(GUIService.getNewForeground());
        drawLogo(g2);

        int startY = GUIService.getHEIGHT()/2 + GUIService.getMENU_START_Y();
        int spacing = GUIService.getMENU_SPACING();

        for(int i = 0; i < options.length; i++) {
            int textWidth = g2.getFontMetrics().stringWidth(options[i]);
            int x = (GUIService.getWIDTH() - textWidth)/2;
            int y = startY + i * spacing;

            boolean isHovered = GUIService.getHITBOX(y).contains(mouse.getX(), mouse.getY());

            g2.setColor(isHovered ? Color.WHITE : GUIService.getNewForeground());
            g2.drawString(options[i], x + GUIService.getGRAPHICS_OFFSET(), y);

            if(isHovered && lastHoveredIndex != i) {
                guiService.getFx().play(BooleanService.getRandom(1, 2));
                lastHoveredIndex = i;
            }
        }
    }

    public void handleMenuInput() {
        if(!mouse.wasPressed()) return;

        int startY = GUIService.getHEIGHT()/2 + GUIService.getMENU_START_Y();
        int spacing = GUIService.getMENU_SPACING();

        for(int i = 0; i < optionsMenu.length; i++) {
            int y = startY + i * spacing;
            boolean isHovered = GUIService.getHITBOX(y).contains(mouse.getX(), mouse.getY());

            if(isHovered) {
                guiService.getFx().play(3);
                switch (i) {
                    case 0 -> gameService.startNewGame();
                    case 1 -> System.exit(0);
                }
                break;
            }
        }
    }
}
