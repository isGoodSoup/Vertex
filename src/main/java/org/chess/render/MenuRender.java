package org.chess.render;

import org.chess.entities.Board;
import org.chess.enums.GameState;
import org.chess.gui.Mouse;
import org.chess.service.BooleanService;
import org.chess.service.GUIService;
import org.chess.service.GameService;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class MenuRender {

    public static final String[] optionsMenu = { "PLAY AGAINST", "RULES",
            "EXIT" };
    public static final String[] optionsMode = { "PLAYER", "AI" };
    public static final String[] optionsTweaks = { "RULES", "Undo Moves",
            "Promotion", "Easy Mode", "Chaos Mode", "Testing", "Castling",
            "En Passant"};
    private static final String ENABLE = "Enable ";
    private final BufferedImage TOGGLE_ON;
    private final BufferedImage TOGGLE_OFF;
    private int lastHoveredIndex = -1;
    private FontMetrics fontMetrics;

    private final GUIService guiService;
    private final Mouse mouse;
    private final GameService gameService;

    public MenuRender(GUIService guiService, GameService gameService, Mouse mouse) {
        this.guiService = guiService;
        this.gameService = gameService;
        this.mouse = mouse;

        try {
            TOGGLE_ON = guiService.getImage("/ui/toggle_on");
            TOGGLE_OFF = guiService.getImage("/ui/toggle_off");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private int getOptionsStartY() {
        return 100 + GUIService.getFontBold(32).getSize() + 8;
    }

    private boolean getOptionState(String option) {
        return switch(option) {
            case "Undo Moves" -> BooleanService.canUndoMoves;
            case "Promotion" -> BooleanService.canPromote;
            case "Easy Mode" -> BooleanService.isEasyModeActive;
            case "Chaos Mode" -> BooleanService.isChaosActive;
            case "Testing" -> BooleanService.isTestingToggle;
            case "Castling" -> BooleanService.isCastlingActive;
            case "En Passant" -> BooleanService.isEnPassantActive;
            default -> false;
        };
    }

    private void toggleOption(String option) {
        switch(option) {
            case "Undo Moves" -> BooleanService.canUndoMoves ^= true;
            case "Promotion" -> BooleanService.canPromote ^= true;
            case "Easy Mode" -> BooleanService.isEasyModeActive ^= true;
            case "Chaos Mode" -> BooleanService.isChaosActive ^= true;
            case "Testing" -> BooleanService.isTestingToggle ^= true;
            case "Castling" -> BooleanService.isCastlingActive ^= true;
            case "En Passant" -> BooleanService.isEnPassantActive ^= true;
        }
    }

    private void drawToggle(Graphics2D g2, BufferedImage image, int x, int y,
                            int width, int height) {
        g2.drawImage(image, x, y, width, height, null);
    }

    public static void drawRandomBackground(boolean isColor) {
        GUIService.setNewBackground(isColor ? Board.getEven() : Board.getOdd());
        GUIService.setNewForeground(isColor ? Board.getOdd() : Board.getEven());
    }

    private static void drawLogo(Graphics2D g2) {
        if(GUIService.getLogo() == null) return;

        int boardWidth = Board.getSquare() * 8;
        int boardCenterX = GUIService.getEXTRA_WIDTH() + boardWidth/2;
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

    public void drawOptionsMenu(Graphics2D g2, String[] options) {
        g2.setFont(GUIService.getFont(32));
        fontMetrics = g2.getFontMetrics();
        g2.setColor(GUIService.getNewForeground());
        int textWidth = fontMetrics.stringWidth(options[0]);
        int boardWidth = Board.getSquare() * 8;
        int totalWidth = boardWidth + 2 * GUIService.getEXTRA_WIDTH();
        int centerX = totalWidth/2;
        int headerTextWidth = g2.getFontMetrics().stringWidth(options[0]);
        int y = 100;
        g2.drawString(options[0], centerX - headerTextWidth/2, y);

        int lineHeight = g2.getFontMetrics().getHeight() + 6;
        y += lineHeight;

        String enabledOption = "";
        for (int i = 1; i < options.length; i++) {
            boolean isHovered = GUIService.getHITBOX(y).contains(mouse.getX(),
                    mouse.getY());

            if(i == options.length - 1) {
                enabledOption = options[i];
            } else {
                enabledOption = ENABLE + options[i];
            }

            int optionWidth = g2.getFontMetrics().stringWidth(enabledOption);
            int x = 100;

            g2.setFont(GUIService.getFontBold(24));
            g2.drawString(enabledOption, x, y);

            int toggleWidth = TOGGLE_ON.getWidth()/2;
            int toggleHeight = TOGGLE_ON.getHeight()/2;
            int toggleX = centerX + 200;
            int toggleY = y - toggleHeight + 16;
            boolean isEnabled = getOptionState(options[i]);
            BufferedImage toggleImage = isEnabled ? TOGGLE_ON : TOGGLE_OFF;

            drawToggle(g2, toggleImage, toggleX, toggleY,
                    toggleWidth, toggleHeight);
            y += lineHeight;
        }
    }

    public void handleOptionsInput() {
        if (!mouse.wasPressed()) { return; }

        int y = 100 + GUIService.getFontBold(32).getSize() + 16;
        int lineHeight = GUIService.getFontBold(32).getSize() + 8;

        int boardWidth = Board.getSquare() * 8;
        int totalWidth = boardWidth + 2 * GUIService.getEXTRA_WIDTH();
        int centerX = totalWidth/2;

        int toggleWidth = TOGGLE_ON.getWidth()/2;
        int toggleHeight = TOGGLE_ON.getHeight()/2;
        int toggleX = centerX + 200;

        for (int i = 1; i < optionsTweaks.length; i++) {
            String option = optionsTweaks[i];

            Rectangle textHitbox = GUIService.getHITBOX(y);
            Rectangle toggleHitbox = new Rectangle(
                    toggleX,
                    y - toggleHeight + 8,
                    toggleWidth,
                    toggleHeight
            );

            boolean clickedText = textHitbox.contains(mouse.getX(), mouse.getY());
            boolean clickedToggle = toggleHitbox.contains(mouse.getX(), mouse.getY());
            if(clickedText || clickedToggle) {
                guiService.getFx().play(0);
                if(option.equals("Back")) {
                    GameService.setState(GameState.MENU);
                } else {
                    toggleOption(option);
                }
                break;
            }
            y += lineHeight;
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
                guiService.getFx().play(0);
                switch (i) {
                    case 0 -> gameService.startNewGame();
                    case 1 -> gameService.optionsMenu();
                    case 2 -> System.exit(0);
                }
                break;
            }
        }
    }
}
