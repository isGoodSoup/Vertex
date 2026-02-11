package org.chess.render;

import org.chess.entities.Board;
import org.chess.input.Mouse;
import org.chess.input.MoveManager;
import org.chess.service.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class MenuRender {
    public static final String[] optionsMenu = { "PLAY AGAINST", "RULES", "EXIT" };
    public static final String[] optionsMode = { "PLAYER", "AI" };
    public static final String[] optionsTweaks = { "RULES",
            "Promotion", "Protanopia", "Training Mode", "Continue", "Tick",
            "Castling",
            "En Passant", "Timer", "Stopwatch", "Chaos Mode", "Testing",
            "Undo Moves", "Reset Table"};
    private static final String ENABLE = "Enable ";
    private static final int OPTION_X = 100;
    private static final int OPTION_Y = 80;
    private final BufferedImage TOGGLE_ON;
    private final BufferedImage TOGGLE_OFF;
    private final BufferedImage TOGGLE_ON_HIGHLIGHTED;
    private final BufferedImage TOGGLE_OFF_HIGHLIGHTED;
    private int lastHoveredIndex = -1;
    private final int OFFSET_X;
    private FontMetrics fontMetrics;
    private int currentPage = 1;

    private final GameService gameService;
    private final BoardService boardService;
    private final MoveManager moveManager;
    private final GUIService guiService;
    private final Mouse mouse;

    public MenuRender(GUIService guiService, GameService gameService,
                      BoardService boardService, MoveManager moveManager,
                      Mouse mouse) {
        this.guiService = guiService;
        this.gameService = gameService;
        this.boardService = boardService;
        this.moveManager = moveManager;
        this.mouse = mouse;

        try {
            TOGGLE_ON = guiService.getImage("/ui/toggle_on");
            TOGGLE_OFF = guiService.getImage("/ui/toggle_off");
            TOGGLE_ON_HIGHLIGHTED = guiService.getImage("/ui/toggle_on-h");
            TOGGLE_OFF_HIGHLIGHTED = guiService.getImage("/ui/toggle_off-h");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        OFFSET_X = GUIService.getWIDTH()/2 - 100
                + GUIService.getGRAPHICS_OFFSET() - 30;
    }

    public int getOFFSET_X() {
        return OFFSET_X;
    }

    private int getOptionsStartY() {
        return 100 + GUIService.getFontBold(32).getSize() + 8;
    }

    private boolean getOptionState(String option) {
        return switch(option) {
            case "Promotion" -> BooleanService.canPromote;
            case "Protanopia" -> BooleanService.canBeColorblind;
            case "Training Mode" -> BooleanService.canTrain;
            case "Continue" -> BooleanService.canContinue;
            case "Tick" -> BooleanService.canTick;
            case "Castling" -> BooleanService.canDoCastling;
            case "En Passant" -> BooleanService.canDoEnPassant;
            case "Timer" -> BooleanService.canTime;
            case "Stopwatch" -> BooleanService.canStopwatch;
            case "Testing" -> BooleanService.canDoTest;
            case "Chaos Mode" -> BooleanService.canDoChaos;
            case "Undo Moves" -> BooleanService.canUndoMoves;
            case "Reset Table" -> BooleanService.canResetTable;
            default -> false;
        };
    }

    public void toggleOption(String option) {
        switch(option) {
            case "Promotion" -> BooleanService.canPromote ^= true;
            case "Protanopia" -> BooleanService.canBeColorblind ^= true;
            case "Training Mode" -> BooleanService.canTrain ^= true;
            case "Continue" -> BooleanService.canContinue ^= true;
            case "Tick" -> BooleanService.canTick ^= true;
            case "Castling" -> BooleanService.canDoCastling ^= true;
            case "En Passant" -> BooleanService.canDoEnPassant ^= true;
            case "Timer" -> {
                BooleanService.canTime ^= true;
                BooleanService.canStopwatch = false;
            }
            case "Stopwatch" -> {
                BooleanService.canStopwatch ^= true;
                BooleanService.canTime = false;
            }
            case "Testing" -> BooleanService.canDoTest ^= true;
            case "Chaos Mode" -> BooleanService.canDoChaos ^= true;
            case "Undo Moves" -> BooleanService.canUndoMoves ^= true;
            case "Reset Table" -> BooleanService.canResetTable ^= true;
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

            boolean isHovered =
                    GUIService.getHITBOX(OFFSET_X, y).contains(mouse.getX(),
                            mouse.getY());
            boolean isSelected = (i == moveManager.getSelectedIndexY());

            g2.setColor(isSelected ? Color.YELLOW : isHovered ? Color.WHITE : GUIService.getNewForeground());
            g2.drawString(options[i], x + GUIService.getGRAPHICS_OFFSET(), y);

            if(isHovered && lastHoveredIndex != i) {
                guiService.getFx().play(BooleanService.getRandom(1, 2));
                lastHoveredIndex = i;
            }
        }
    }

    public void drawOptionsMenu(Graphics2D g2, String[] options) {
        g2.setFont(GUIService.getFont(32));
        updatePage();
        fontMetrics = g2.getFontMetrics();
        g2.setColor(GUIService.getNewForeground());
        int textWidth = fontMetrics.stringWidth(options[0]);
        int boardWidth = Board.getSquare() * 8;
        int totalWidth = boardWidth + 2 * GUIService.getEXTRA_WIDTH();
        int centerX = totalWidth/2;
        int headerTextWidth = fontMetrics.stringWidth(options[0]);
        int y = OPTION_Y;

        int itemsPerPage = 8;
        int numberOfToggles = options.length - 1;
        int totalPages = (numberOfToggles + itemsPerPage - 1) / itemsPerPage;
        String pageText = "< " + currentPage + "/" + totalPages + " >";
        int pageTextWidth = fontMetrics.stringWidth(pageText);
        int pageY = y + fontMetrics.getHeight() + 350;

        g2.drawString(options[0], centerX - headerTextWidth/2, y);
        g2.drawString(pageText, centerX - pageTextWidth/2, pageY);

        int lineHeight = fontMetrics.getHeight() + 4;
        y += lineHeight;

        int startIndex = (currentPage - 1) * itemsPerPage + 1;
        int endIndex = Math.min(startIndex + itemsPerPage, options.length);

        String enabledOption = "";
        for(int i = startIndex; i < endIndex; i++) {
            enabledOption = ENABLE + options[i];

            int optionWidth = fontMetrics.stringWidth(enabledOption);
            int x = OPTION_X;

            g2.setFont(GUIService.getFontBold(24));
            int toggleWidth = TOGGLE_ON.getWidth()/2;
            int toggleHeight = TOGGLE_ON.getHeight()/2;
            int toggleX = centerX + 200;
            int toggleY = y - toggleHeight + 16;
            boolean isHovered = GUIService.getHITBOX(OFFSET_X, y).contains(mouse.getX(),
                            mouse.getY());
            boolean isSelected = (i == moveManager.getSelectedIndexY());
            boolean isEnabled = getOptionState(options[i]);

            if(moveManager.getSelectedIndexY() > options.length
                    || moveManager.getSelectedIndexY() < 0) {
                moveManager.setSelectedIndexY(0);
            }

            g2.setColor(GUIService.getNewForeground());
            g2.drawString(enabledOption, x, y);

            BufferedImage toggleImage = isEnabled
                    ? (isSelected || isHovered ? TOGGLE_ON_HIGHLIGHTED : TOGGLE_ON)
                    : (isSelected || isHovered ? TOGGLE_OFF_HIGHLIGHTED :
                    TOGGLE_OFF);

            drawToggle(g2, toggleImage, toggleX, toggleY,
                    toggleWidth, toggleHeight);
            y += lineHeight;
        }
    }

    private boolean updatePage() {
        int itemsPerPage = 8;
        int selectedX = moveManager.getSelectedIndexX();
        int totalPages = (optionsTweaks.length - 1 + itemsPerPage - 1) / itemsPerPage;
        int newPage = Math.max(1, Math.min(selectedX, totalPages));
        if(newPage != currentPage) {
            currentPage = newPage;
            moveManager.setSelectedIndexY(0);
            return true;
        }
        return false;
    }

    public void previousPage() {
        int selectedX = moveManager.getSelectedIndexX();
        if (selectedX > 1) moveManager.setSelectedIndexX(selectedX - 1);
        updatePage();
    }

    public void nextPage() {
        int selectedX = moveManager.getSelectedIndexX();
        int itemsPerPage = 8;
        int totalPages = (optionsTweaks.length - 1 + itemsPerPage - 1) / itemsPerPage;

        if (selectedX < totalPages) moveManager.setSelectedIndexX(selectedX + 1);
        updatePage();
    }

    public void handleOptionsInput() {
        if(!mouse.wasPressed()) { return; }

        int lineHeight = fontMetrics.getHeight() + 4;
        int y = OPTION_Y + lineHeight;

        int boardWidth = Board.getSquare() * 8;
        int totalWidth = boardWidth + 2 * GUIService.getEXTRA_WIDTH();
        int centerX = totalWidth / 2;
        int toggleWidth = TOGGLE_ON.getWidth() / 2;
        int toggleHeight = TOGGLE_ON.getHeight() / 2;
        int toggleX = centerX + 200;

        for (int i = 1; i < optionsTweaks.length; i++) {
            String option = optionsTweaks[i];
            String enabledOption = ENABLE + option;
            int textX = OPTION_X;
            int textY = y;
            int textWidth = fontMetrics.stringWidth(enabledOption);

            Rectangle toggleHitbox = new Rectangle(
                    toggleX,
                    textY - toggleHeight + 16,
                    toggleWidth,
                    toggleHeight
            );

            if (toggleHitbox.contains(mouse.getX(), mouse.getY())) {
                guiService.getFx().play(0);
                toggleOption(option);
                break;
            }
            y += lineHeight;
        }
    }

    public void handleMenuInput() {
        if(!mouse.wasPressed()) { return; }

        int startY = GUIService.getHEIGHT()/2 + GUIService.getMENU_START_Y();
        int spacing = GUIService.getMENU_SPACING();

        for(int i = 0; i < optionsMenu.length; i++) {
            int y = startY + i * spacing;
            boolean isHovered =
                    GUIService.getHITBOX(OFFSET_X, y).contains(mouse.getX(),
                            mouse.getY());

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
