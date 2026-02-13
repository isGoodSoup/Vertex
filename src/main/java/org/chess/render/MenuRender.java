package org.chess.render;

import org.chess.entities.Achievement;
import org.chess.entities.Board;
import org.chess.enums.ColorblindType;
import org.chess.gui.Colors;
import org.chess.input.MenuInput;
import org.chess.input.Mouse;
import org.chess.input.MoveManager;
import org.chess.service.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class MenuRender {
    public static final String[] optionsMenu = { "NEW GAME",
            "ACHIEVEMENTS", "RULES", "EXIT" };
    public static final String[] optionsMode = { "PLAYER", "AI" };
    public static final String[] optionsTweaks = {
            "RULES",
            "Dark Mode",
            "Promotion",
            "Achievements",
            "Training Mode",
            "Continue",
            "Castling",
            "En Passant",
            "Timer",
            "Stopwatch",
            "Chaos Mode",
            "Sandbox Mode",
            "Undo Moves",
            "Reset Table",
            "Colorblind Mode",
            "Themes"
    };
    public static String ENABLE = "Enable ";
    private static final int OPTION_X = 100;
    private static final int OPTION_Y = 160;
    private static final float SCALE = 1.5f;
    private BufferedImage DARK_MODE_ON;
    private BufferedImage DARK_MODE_OFF;
    private BufferedImage DARK_MODE_ON_HIGHLIGHTED;
    private BufferedImage DARK_MODE_OFF_HIGHLIGHTED;
    private BufferedImage TOGGLE_ON;
    private BufferedImage TOGGLE_OFF;
    private BufferedImage TOGGLE_ON_HIGHLIGHTED;
    private BufferedImage TOGGLE_OFF_HIGHLIGHTED;
    private static ColorblindType cb;
    private int lastHoveredIndex = -1;
    private int scrollOffset = 0;
    private static int totalWidth;
    private static FontMetrics fm;
    private int currentPage = 1;

    private static RenderContext render;
    private GameService gameService;
    private BoardService boardService;
    private MoveManager moveManager;
    private GUIService guiService;
    private Mouse mouse;
    private MenuInput menuInput;
    private AchievementSprites sprites;

    public MenuRender(RenderContext render) {
        MenuRender.render = render;
        cb = ColorblindType.PROTANOPIA;
    }

    public void init() {
        this.sprites = new AchievementSprites(guiService);
        this.menuInput = new MenuInput(render, this, guiService, gameService,
                boardService, moveManager, mouse);
        try {
            DARK_MODE_ON = guiService.getImage("/ui/dark-mode_on");
            DARK_MODE_OFF = guiService.getImage("/ui/dark-mode_off");
            DARK_MODE_ON_HIGHLIGHTED = guiService.getImage("/ui/dark-mode_on-h");
            DARK_MODE_OFF_HIGHLIGHTED = guiService.getImage("/ui/dark-mode_off-h");
            TOGGLE_ON = guiService.getImage("/ui/toggle_on");
            TOGGLE_OFF = guiService.getImage("/ui/toggle_off");
            TOGGLE_ON_HIGHLIGHTED = guiService.getImage("/ui/toggle_on-h");
            TOGGLE_OFF_HIGHLIGHTED = guiService.getImage("/ui/toggle_off-h");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        DARK_MODE_ON = Colorblindness.filter(DARK_MODE_ON);
        DARK_MODE_OFF = Colorblindness.filter(DARK_MODE_OFF);
        DARK_MODE_ON_HIGHLIGHTED = Colorblindness.filter(DARK_MODE_ON_HIGHLIGHTED);
        DARK_MODE_OFF_HIGHLIGHTED =
                Colorblindness.filter(DARK_MODE_OFF_HIGHLIGHTED);
        TOGGLE_ON = Colorblindness.filter(TOGGLE_ON);
        TOGGLE_OFF = Colorblindness.filter(TOGGLE_OFF);
        TOGGLE_ON_HIGHLIGHTED = Colorblindness.filter(TOGGLE_ON_HIGHLIGHTED);
        TOGGLE_OFF_HIGHLIGHTED = Colorblindness.filter(TOGGLE_OFF_HIGHLIGHTED);
        AchievementService.lockAllAchievements();
    }

    public GameService getGameService() {
        return gameService;
    }

    public void setGameService(GameService gameService) {
        this.gameService = gameService;
    }

    public BoardService getBoardService() {
        return boardService;
    }

    public void setBoardService(BoardService boardService) {
        this.boardService = boardService;
    }

    public MoveManager getMoveManager() {
        return moveManager;
    }

    public void setMoveManager(MoveManager moveManager) {
        this.moveManager = moveManager;
    }

    public GUIService getGuiService() {
        return guiService;
    }

    public void setGuiService(GUIService guiService) {
        this.guiService = guiService;
    }

    public Mouse getMouse() {
        return mouse;
    }

    public void setMouse(Mouse mouse) {
        this.mouse = mouse;
    }

    public MenuInput getMenuInput() {
        return menuInput;
    }

    public static ColorblindType getCb() {
        return cb;
    }

    public static void setCb(ColorblindType cb) {
        MenuRender.cb = cb;
    }

    public static int getCenterX(int containerWidth, int elementWidth) {
        return render.getOffsetX()
                + (containerWidth - elementWidth)/2;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getTotalWidth() {
        return totalWidth = render.scale(RenderContext.BASE_WIDTH);
    }

    public FontMetrics getFontMetrics() {
        return fm;
    }

    public static int getOPTION_X() {
        return OPTION_X;
    }

    public static int getOPTION_Y() {
        return OPTION_Y;
    }

    private boolean getOptionState(String option) {
        return switch(option) {
            case "Dark Mode" -> BooleanService.isDarkMode;
            case "Promotion" -> BooleanService.canPromote;
            case "Achievements" -> BooleanService.canDoAchievements;
            case "Training Mode" -> BooleanService.canTrain;
            case "Continue" -> BooleanService.canContinue;
            case "Castling" -> BooleanService.canDoCastling;
            case "En Passant" -> BooleanService.canDoEnPassant;
            case "Timer" -> BooleanService.canTime;
            case "Stopwatch" -> BooleanService.canStopwatch;
            case "Sandbox Mode" -> BooleanService.canSandbox;
            case "Chaos Mode" -> BooleanService.canDoChaos;
            case "Undo Moves" -> BooleanService.canUndoMoves;
            case "Reset Table" -> BooleanService.canResetTable;
            case "Colorblind Mode" -> BooleanService.canBeColorblind;
            case "Themes" -> BooleanService.canTheme;
            default -> false;
        };
    }

    public void toggleOption(String option) {
        switch(option) {
            case "Dark Mode" -> {
                BooleanService.isDarkMode ^= true;
                Colors.toggleDarkTheme();
            }
            case "Promotion" -> BooleanService.canPromote ^= true;
            case "Achievements" -> BooleanService.canDoAchievements ^= true;
            case "Training Mode" -> BooleanService.canTrain ^= true;
            case "Continue" -> BooleanService.canContinue ^= true;
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
            case "Sandbox Mode" -> BooleanService.canSandbox ^= true;
            case "Chaos Mode" -> BooleanService.canDoChaos ^= true;
            case "Undo Moves" -> BooleanService.canUndoMoves ^= true;
            case "Reset Table" -> BooleanService.canResetTable ^= true;
            case "Colorblind Mode" -> BooleanService.canBeColorblind ^= true;
            case "Themes" -> BooleanService.canTheme ^= true;
        }
    }

    private void drawToggle(Graphics2D g2, BufferedImage image, int x, int y,
                            int width, int height) {
        g2.drawImage(image, x, y, width, height, null);
    }

    private static void drawLogo(Graphics2D g2, int containerWidth) {
        if(GUIService.getLogo() == null) { return; }
        BufferedImage img = BooleanService.isDarkMode ?
                GUIService.getLogo_v2() : Colorblindness.filter(GUIService.getLogo());
        int boardWidth = Board.getSquare() * 8;
        int boardCenterX = render.getOffsetX() + render.scale(
                RenderContext.BASE_WIDTH) * 2 + boardWidth/2;
        int logoWidth = (int) (GUIService.getLogo().getWidth()/SCALE);
        int logoHeight = (int) (GUIService.getLogo().getHeight()/SCALE);
        int x = getCenterX(containerWidth, logoWidth);
        int y = render.getOffsetY() + render.scale(RenderContext.BASE_HEIGHT)/10;
        g2.drawImage(img, x, y, logoWidth, logoHeight, null);
    }

    public void drawGraphics(Graphics2D g2, String[] options) {
        g2.setColor(Colorblindness.filter(Colors.BACKGROUND));
        g2.fillRect(0, 0, getTotalWidth(), render.scale(RenderContext.BASE_HEIGHT));

        g2.setFont(GUIService.getFont(GUIService.getMENU_FONT()));
        drawLogo(g2, getTotalWidth());

        int startY = render.scale(RenderContext.BASE_HEIGHT)/2 + render.scale(GUIService.getMENU_START_Y());
        int spacing = render.scale(GUIService.getMENU_SPACING());

        for(int i = 0; i < options.length; i++) {
            String optionText = options[i];
            fm = g2.getFontMetrics();
            int textWidth = fm.stringWidth(optionText);

            int x = getCenterX(getTotalWidth(), textWidth);
            int y = render.getOffsetY() + startY + i * spacing;

            Rectangle hitbox = new Rectangle(
                    x,
                    y - fm.getAscent(),
                    textWidth,
                    fm.getHeight()
            );

            boolean isHovered = hitbox.contains(mouse.getX(), mouse.getY())
                    || (i == moveManager.getSelectedIndexY());

            Color foreground = Colorblindness.filter(Colors.FOREGROUND);
            Color textColor = isHovered ? Color.YELLOW : foreground;

            g2.setColor(textColor);
            g2.drawString(optionText, x, y);

            if(isHovered && lastHoveredIndex != i) {
                guiService.getFx().play(BooleanService.getRandom(1, 2));
                lastHoveredIndex = i;
            }
        }
    }

    public void drawOptionsMenu(Graphics2D g2, String[] options) {
        g2.setColor(Colorblindness.filter(Colors.BACKGROUND));
        g2.fillRect(0, 0, getTotalWidth(), render.scale(RenderContext.BASE_HEIGHT));

        g2.setFont(GUIService.getFontBold(GUIService.getMENU_FONT()));
        fm = g2.getFontMetrics();

        int headerY = render.getOffsetY() + render.scale(OPTION_Y);
        int headerWidth = fm.stringWidth(options[0]);
        g2.setColor(BooleanService.canBeColorblind ?
                Colorblindness.filter(Colors.FOREGROUND)
                : Colors.FOREGROUND);
        g2.drawString(options[0],
                getCenterX(getTotalWidth(), headerWidth),
                headerY);

        int startY = headerY + render.scale(90);
        int lineHeight = fm.getHeight() + render.scale(10);
        int itemsPerPage = 8;

        int startIndex = (currentPage - 1) * itemsPerPage + 1;
        int endIndex = Math.min(startIndex + itemsPerPage, optionsTweaks.length);

        int gap = render.scale(100);
        int maxRowWidth = 0;
        g2.setFont(GUIService.getFont(GUIService.getMENU_FONT()));

        for(int i = startIndex; i < endIndex; i++) {
            String enabledOption = ENABLE + options[i];
            int textWidth = g2.getFontMetrics().stringWidth(enabledOption.toUpperCase());
            int toggleWidth = render.scale(TOGGLE_ON.getWidth()/2);
            int rowWidth = textWidth + gap + toggleWidth;
            if(rowWidth > maxRowWidth) maxRowWidth = rowWidth;
        }

        for(int i = startIndex; i < endIndex; i++) {
            String enabledOption = ENABLE + options[i];
            int textWidth = g2.getFontMetrics().stringWidth(enabledOption);
            int toggleWidth = render.scale(TOGGLE_ON.getWidth()/2);
            int toggleHeight = render.scale(TOGGLE_ON.getHeight()/2);

            int blockX = getCenterX(getTotalWidth(), maxRowWidth);
            int textX = blockX;
            int toggleX = blockX + maxRowWidth - toggleWidth;
            int toggleY = startY - toggleHeight;

            Rectangle toggleHitbox = new Rectangle(
                    toggleX,
                    toggleY,
                    toggleWidth,
                    toggleHeight
            );

            boolean isHovered = toggleHitbox.contains(mouse.getX(),
                    mouse.getY()) || (i == moveManager.getSelectedIndexY());
            boolean isEnabled = getOptionState(options[i]);

            g2.setColor(Colorblindness.filter(Colors.FOREGROUND));
            g2.drawString(enabledOption.toUpperCase(), textX,
                    render.getOffsetY() + startY);

            BufferedImage toggleImage;
            if(options[i].equals("Dark Mode")) {
                toggleImage = isEnabled
                        ? (isHovered ? DARK_MODE_ON_HIGHLIGHTED : DARK_MODE_ON)
                        : (isHovered ? DARK_MODE_OFF_HIGHLIGHTED : DARK_MODE_OFF);
            } else {
                toggleImage = isEnabled
                        ? (isHovered ? TOGGLE_ON_HIGHLIGHTED : TOGGLE_ON)
                        : (isHovered ? TOGGLE_OFF_HIGHLIGHTED : TOGGLE_OFF);
            }
            drawToggle(g2, toggleImage, render.getOffsetX() + toggleX,
                    render.getOffsetY() + toggleY, toggleWidth, toggleHeight);

            startY += lineHeight;
        }
    }

    public void drawAchievementsMenu(Graphics2D g2) {
        Collection<Achievement> achievements =
                AchievementService.getAllAchievements();
        List<Achievement> list = new ArrayList<>(achievements);
        list.sort(Comparator.comparingInt(a -> a.getId().ordinal()));

        String text = "ACHIEVEMENTS";
        int headerY = render.getOffsetY() + render.scale(OPTION_Y);
        int headerWidth = fm.stringWidth(text);
        g2.setFont(GUIService.getFontBold(GUIService.getMENU_FONT()));
        g2.setColor(BooleanService.canBeColorblind ?
                Colorblindness.filter(Colors.FOREGROUND)
                : Colors.FOREGROUND);
        g2.drawString(text,
                getCenterX(getTotalWidth(), headerWidth),
                headerY);

        int stroke = 4;
        int spacing = 25;
        int startY = headerY + spacing * 2;
        int width = RenderContext.BASE_WIDTH/2;
        int height = 100, arcWidth = 32, arcHeight = 32;
        int x = getCenterX(getTotalWidth(), width);
        boolean hasBackground = true;

        int itemsPerPage = MoveManager.getITEMS_PER_PAGE();
        int start = (currentPage - 1) * itemsPerPage;
        int end = Math.min(start + itemsPerPage, list.size());

        BufferedImage img = null;
        for (int i = start; i < end; i++) {
            Achievement a = list.get(i);
            Rectangle hitbox = new Rectangle(
                    x, startY, width, height
            );

            boolean isHovered = hitbox.contains(mouse.getX(), mouse.getY());

            int textX = x + render.scale(110);
            int titleY = startY + render.scale(60);
            int descY = titleY;
            g2.setFont(GUIService.getFont(GUIService.getMENU_FONT()));
            g2.setColor(Colorblindness.filter(Color.WHITE));

            if(isHovered) {
                guiService.drawBox(g2, stroke, x, startY,
                        width, height, arcWidth, arcHeight, hasBackground, true);
                g2.drawString(a.getId().getDescription(), textX, descY);
            } else {
                guiService.drawBox(g2, stroke, x, startY,
                        width, height, arcWidth, arcHeight, hasBackground, false);
                g2.drawString(a.getId().getTitle(), textX, titleY);
            }

            BooleanService.isAchievementLocked = true;
            img = sprites.getSprite(a);

            if (img != null && !a.isUnlocked()) {
                img = AchievementLock.filter(img);
            }

            if (img != null) {
                int iconSize = render.scale(64);
                int iconX = x + render.scale(20);
                int iconY = startY + (height - iconSize) / 2;

                g2.drawImage(img,
                        iconX,
                        iconY,
                        iconSize,
                        iconSize,
                        null);
            }
            startY += height + spacing;
        }
    }


    public BufferedImage getSprite(int i) {
        return switch (i) {
            case 0 -> DARK_MODE_ON;
            case 1 -> DARK_MODE_OFF;
            case 2 -> DARK_MODE_ON_HIGHLIGHTED;
            case 3 -> DARK_MODE_OFF_HIGHLIGHTED;
            case 4 -> TOGGLE_ON;
            case 5 -> TOGGLE_OFF;
            case 6 -> TOGGLE_ON_HIGHLIGHTED;
            case 7 -> TOGGLE_OFF_HIGHLIGHTED;
            default -> null;
        };
    }
}
