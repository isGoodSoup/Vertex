package org.chess.render;

import org.chess.entities.Achievement;
import org.chess.entities.Board;
import org.chess.enums.ColorblindType;
import org.chess.enums.GameSettings;
import org.chess.enums.GameState;
import org.chess.gui.Colors;
import org.chess.input.MouseInput;
import org.chess.input.Mouse;
import org.chess.manager.MovesManager;
import org.chess.records.Save;
import org.chess.service.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class MenuRender {
    public static final String[] optionsMenu = { "NEW GAME", "LOAD SAVE",
            "ACHIEVEMENTS", "SETTINGS", "EXIT" };
    public static final String[] optionsMode = { "PLAYER", "AI" };
    public static final GameSettings[] optionsTweaks = GameSettings.values();
    private static final String SETTINGS = "SETTINGS";
    private static final String ACHIEVEMENTS = "ACHIEVEMENTS";
    public static String ENABLE = "Enable ";
    private static final String CHECKMATE = "Checkmate!";
    private static final int OPTION_X = 100;
    private static final int OPTION_Y = 160;
    private static final float SCALE = 1.5f;
    private transient BufferedImage DARK_MODE_ON;
    private transient BufferedImage DARK_MODE_OFF;
    private transient BufferedImage DARK_MODE_ON_HIGHLIGHTED;
    private transient BufferedImage DARK_MODE_OFF_HIGHLIGHTED;
    private transient BufferedImage TOGGLE_ON;
    private transient BufferedImage TOGGLE_OFF;
    private transient BufferedImage TOGGLE_ON_HIGHLIGHTED;
    private transient BufferedImage TOGGLE_OFF_HIGHLIGHTED;
    private static ColorblindType cb;
    private int lastHoveredIndex = -1;
    private int scrollOffset = 0;
    private static int totalWidth;
    private static FontMetrics fm;
    private int currentPage = 1;

    private static RenderContext render;
    private GameService gameService;
    private BoardService boardService;
    private MovesManager movesManager;
    private GUIService guiService;
    private Mouse mouse;
    private MouseInput mouseInput;
    private AchievementSprites sprites;

    public MenuRender(RenderContext render) {
        MenuRender.render = render;
        cb = ColorblindType.PROTANOPIA;
    }

    public void init() {
        this.sprites = new AchievementSprites(guiService);
        this.mouseInput = new MouseInput(render, this, guiService, gameService,
                boardService, movesManager, mouse);
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

    public MovesManager getMoveManager() {
        return movesManager;
    }

    public void setMoveManager(MovesManager movesManager) {
        this.movesManager = movesManager;
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

    public MouseInput getMenuInput() {
        return mouseInput;
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
                    || (i == movesManager.getSelectedIndexY());

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

    public void drawOptionsMenu(Graphics2D g2, GameSettings[] options) {
        g2.setColor(Colorblindness.filter(Colors.BACKGROUND));
        g2.fillRect(0, 0, getTotalWidth(), render.scale(RenderContext.BASE_HEIGHT));

        g2.setFont(GUIService.getFontBold(GUIService.getMENU_FONT()));
        fm = g2.getFontMetrics();

        String header = SETTINGS;
        int headerY = render.getOffsetY() + render.scale(OPTION_Y);
        int headerWidth = fm.stringWidth(header);
        g2.setColor(Colorblindness.filter(Colors.FOREGROUND));
        g2.drawString(header, getCenterX(getTotalWidth(), headerWidth),headerY);

        int startY = headerY + render.scale(90);
        int lineHeight = fm.getHeight() + render.scale(10);
        int itemsPerPage = 8;

        int startIndex = (currentPage - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, options.length);

        int gap = render.scale(100);
        int maxRowWidth = 0;
        g2.setFont(GUIService.getFont(GUIService.getMENU_FONT()));

        for(int i = startIndex; i < endIndex; i++) {
            GameSettings option = options[i];
            String enabledOption = ENABLE + option.getLabel();
            int textWidth =
                    g2.getFontMetrics().stringWidth(enabledOption.toUpperCase());
            int toggleWidth = render.scale(TOGGLE_ON.getWidth()/2);
            int rowWidth = textWidth + gap + toggleWidth;
            if(rowWidth > maxRowWidth) maxRowWidth = rowWidth;
        }

        for(int i = startIndex; i < endIndex; i++) {
            GameSettings option = options[i];
            String enabledOption = ENABLE + option.getLabel();
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
                    mouse.getY()) || (i == movesManager.getSelectedIndexY());
            boolean isEnabled = option.get();

            g2.setColor(Colorblindness.filter(Colors.FOREGROUND));
            g2.drawString(enabledOption.toUpperCase(), textX,
                    render.getOffsetY() + startY);

            BufferedImage toggleImage;
            if(options[i] == GameSettings.DARK_MODE) {
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
                boardService.getServiceFactory().getAchievementService().getAchievementList();
        List<Achievement> list = new ArrayList<>(achievements);
        list.sort(Comparator.comparingInt(a -> a.getId().ordinal()));

        String text = ACHIEVEMENTS;
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

        int itemsPerPage = MovesManager.getITEMS_PER_PAGE();
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
                GUIService.drawBox(g2, stroke, x, startY,
                        width, height, arcWidth, arcHeight, hasBackground, true);
                g2.drawString(a.getId().getDescription(), textX, descY);
            } else {
                GUIService.drawBox(g2, stroke, x, startY,
                        width, height, arcWidth, arcHeight, hasBackground, false);
                g2.drawString(a.getId().getTitle(), textX, titleY);
            }

            BooleanService.isAchievementLocked = true;
            img = AchievementSprites.getSprite(a);

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

    public void drawSavesMenu(Graphics2D g2) {
        List<Save> saves = gameService.getSaveManager().getSaves();
        String text = "SAVE FILES";

        g2.setFont(GUIService.getFontBold(GUIService.getMENU_FONT()));
        FontMetrics fm = g2.getFontMetrics();

        int headerY = render.getOffsetY() + render.scale(OPTION_Y);
        int headerWidth = fm.stringWidth(text);
        g2.setColor(BooleanService.canBeColorblind ?
                Colorblindness.filter(Colors.FOREGROUND)
                : Colors.FOREGROUND);
        g2.drawString(text,
                getCenterX(getTotalWidth(), headerWidth),
                headerY);

        if(saves.isEmpty()) {
            return;
        }

        int stroke = 4;
        int spacing = 25;
        int startY = headerY + spacing * 2;
        int width = RenderContext.BASE_WIDTH/2;
        int height = 100, arcWidth = 32, arcHeight = 32;
        int x = getCenterX(getTotalWidth(), width);
        boolean hasBackground = true;

        int itemsPerPage = MovesManager.getITEMS_PER_PAGE();
        int start = (currentPage - 1) * itemsPerPage;
        int end = Math.min(start + itemsPerPage, saves.size());

        BufferedImage img = null;
        for (int i = start; i < end; i++) {
            Save s = saves.get(i);
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
                GUIService.drawBox(g2, stroke, x, startY,
                        width, height, arcWidth, arcHeight, hasBackground,
                        true);
            } else {
                GUIService.drawBox(g2, stroke, x, startY,
                        width, height, arcWidth, arcHeight, hasBackground, false);
            }

            g2.drawString(s.name(), textX, descY);
            startY += height + spacing;
        }
    }

    public void drawSandboxMenu(Graphics2D g2) {

    }

    public void drawCheckmate(Graphics2D g2) {
        if(GameService.getState() != GameState.CHECKMATE) { return; }
        g2.setFont(GUIService.getFontBold(GUIService.getMENU_FONT()));
        FontMetrics fm = g2.getFontMetrics();

        int headerY = render.getOffsetY() + render.scale(200);
        int headerWidth = fm.stringWidth(CHECKMATE);
        g2.setColor(BooleanService.canBeColorblind ?
                Colorblindness.filter(Colors.FOREGROUND)
                : Colors.FOREGROUND);
        g2.drawString(CHECKMATE, getCenterX(getTotalWidth(), headerWidth),headerY);
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
