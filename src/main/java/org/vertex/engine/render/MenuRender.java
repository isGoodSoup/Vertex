package org.vertex.engine.render;

import org.vertex.engine.entities.Achievement;
import org.vertex.engine.entities.Board;
import org.vertex.engine.enums.*;
import org.vertex.engine.gui.Colors;
import org.vertex.engine.input.Mouse;
import org.vertex.engine.input.MouseInput;
import org.vertex.engine.manager.MovesManager;
import org.vertex.engine.records.Save;
import org.vertex.engine.service.BoardService;
import org.vertex.engine.service.BooleanService;
import org.vertex.engine.service.GUIService;
import org.vertex.engine.service.GameService;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

public class MenuRender {
    public static final GameMenu[] MENU = GameMenu.values();
    public static final Games[] GAMES = Games.values();
    public static final GameSettings[] SETTINGS_MENU = GameSettings.values();
    private static final String SETTINGS = "SETTINGS";
    private static final String ACHIEVEMENTS = "ACHIEVEMENTS";
    private static final String SAVES = "SAVE FILES";
    public static String ENABLE = "Enable ";
    private static final String CHECKMATE = "Checkmate!";
    private static final String STALEMATE = "Stalemate";
    private static final int OPTION_X = 100;
    private static final int OPTION_Y = 160;
    private static final float SCALE = 1.5f;
    private transient BufferedImage TOGGLE_ON;
    private transient BufferedImage TOGGLE_OFF;
    private transient BufferedImage TOGGLE_ON_HIGHLIGHTED;
    private transient BufferedImage TOGGLE_OFF_HIGHLIGHTED;
    private transient BufferedImage DARK_MODE_ON;
    private transient BufferedImage DARK_MODE_ON_HIGHLIGHTED;
    private transient BufferedImage HARD_MODE_ON;
    private transient BufferedImage HARD_MODE_ON_HIGHLIGHTED;
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
            TOGGLE_ON = guiService.getImage("/ui/toggle_on");
            TOGGLE_OFF = guiService.getImage("/ui/toggle_off");
            TOGGLE_ON_HIGHLIGHTED = guiService.getImage("/ui/toggle_onh");
            TOGGLE_OFF_HIGHLIGHTED = guiService.getImage("/ui/toggle_offh");
            DARK_MODE_ON = guiService.getImage("/ui/dark-mode_on");
            DARK_MODE_ON_HIGHLIGHTED = guiService.getImage("/ui/dark-mode_onh");
            HARD_MODE_ON = guiService.getImage("/ui/hardmode_on");
            HARD_MODE_ON_HIGHLIGHTED = guiService.getImage("/ui/hardmode_onh");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        TOGGLE_ON = Colorblindness.filter(TOGGLE_ON);
        TOGGLE_OFF = Colorblindness.filter(TOGGLE_OFF);
        TOGGLE_ON_HIGHLIGHTED = Colorblindness.filter(TOGGLE_ON_HIGHLIGHTED);
        TOGGLE_OFF_HIGHLIGHTED = Colorblindness.filter(TOGGLE_OFF_HIGHLIGHTED);
        DARK_MODE_ON = Colorblindness.filter(DARK_MODE_ON);
        DARK_MODE_ON_HIGHLIGHTED = Colorblindness.filter(DARK_MODE_ON_HIGHLIGHTED);
        HARD_MODE_ON = Colorblindness.filter(HARD_MODE_ON);
        HARD_MODE_ON_HIGHLIGHTED =
                Colorblindness.filter(HARD_MODE_ON_HIGHLIGHTED);
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

    public MouseInput getMouseInput() {
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
        BufferedImage img;
        if(Colors.getBackground() == Theme.LEGACY.getBackground()) {
            img = GUIService.getOldLogo();
        } else if(BooleanService.isDarkMode || Colors.getBackground() == Theme.BLACK.getBackground()) {
            img = GUIService.getLogoV2();
        } else {
            img = GUIService.getLogo();
        }

        img = Colorblindness.filter(img);
        int boardWidth = Board.getSquare() * 8;
        int boardCenterX = render.getOffsetX() + render.scale(
                RenderContext.BASE_WIDTH) * 2 + boardWidth/2;
        int logoWidth = (int) (GUIService.getLogo().getWidth()/SCALE);
        int logoHeight = (int) (GUIService.getLogo().getHeight()/SCALE);
        int x = getCenterX(containerWidth, logoWidth);
        int y = render.getOffsetY() + render.scale(RenderContext.BASE_HEIGHT)/6;
        g2.drawImage(img, x, y, logoWidth, logoHeight, null);
    }

    public void drawGraphics(Graphics2D g2, GameMenu[] options) {
        g2.setColor(Colorblindness.filter(Colors.getBackground()));
        g2.fillRect(0, 0, getTotalWidth(), render.scale(RenderContext.BASE_HEIGHT));

        g2.setFont(GUIService.getFont(GUIService.getMENU_FONT()));
        drawLogo(g2, getTotalWidth());

        int startY = render.scale(RenderContext.BASE_HEIGHT)/2 + render.scale(GUIService.getMENU_START_Y());
        int spacing = render.scale(GUIService.getMENU_SPACING());

        for(int i = 0; i < options.length; i++) {
            GameMenu op = options[i];
            String option = op.getLabel();
            fm = g2.getFontMetrics();
            int textWidth = fm.stringWidth(option);

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

            Color foreground = Colorblindness.filter(Colors.getForeground());
            Color textColor = isHovered ? Colors.getHighlight() : foreground;

            g2.setColor(textColor);
            g2.drawString(option, x, y);

            if(isHovered && lastHoveredIndex != i) {
                guiService.getFx().playFX(BooleanService.getRandom(1, 2));
                lastHoveredIndex = i;
            }
        }
    }

    public void drawGamesMenu(Graphics2D g2, Games[] games) {
        g2.setColor(Colorblindness.filter(Colors.getBackground()));
        g2.fillRect(0, 0, getTotalWidth(), render.scale(RenderContext.BASE_HEIGHT));

        g2.setFont(GUIService.getFont(GUIService.getMENU_FONT()));
        drawLogo(g2, getTotalWidth());

        int startY = render.scale(RenderContext.BASE_HEIGHT)/2 + render.scale(GUIService.getMENU_START_Y());
        int spacing = render.scale(GUIService.getMENU_SPACING());

        for(int i = 0; i < games.length; i++) {
            Games op = games[i];
            String option = op.getLabel();
            fm = g2.getFontMetrics();
            int textWidth = fm.stringWidth(option);

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

            Color foreground = Colorblindness.filter(Colors.getForeground());
            Color textColor = isHovered ? Colors.getHighlight() : foreground;

            g2.setColor(textColor);
            g2.drawString(option, x, y);

            if(isHovered && lastHoveredIndex != i) {
                guiService.getFx().playFX(BooleanService.getRandom(1, 2));
                lastHoveredIndex = i;
            }
        }
    }

    public void drawOptionsMenu(Graphics2D g2, GameSettings[] options) {
        g2.setColor(Colorblindness.filter(Colors.getBackground()));
        g2.fillRect(0, 0, getTotalWidth(), render.scale(RenderContext.BASE_HEIGHT));

        int stroke = 4;
        int x = 32, y = 32;
        int arc = 40;

        GUIService.drawBox(g2, stroke, x, y,
                render.scale(RenderContext.BASE_WIDTH - x * 2),
                render.scale(RenderContext.BASE_HEIGHT - y * 2), arc, arc, true,
                false, 255);

        g2.setFont(GUIService.getFont(GUIService.getMENU_FONT()));
        fm = g2.getFontMetrics();

        String header = SETTINGS;
        int headerY = render.getOffsetY() + render.scale(OPTION_Y);
        int headerWidth = fm.stringWidth(header);
        g2.setColor(Colorblindness.filter(Colors.getTheme() == Theme.DEFAULT
                ? Color.WHITE : Colors.getForeground()));
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

            g2.drawString(enabledOption.toUpperCase(), textX,
                    render.getOffsetY() + startY);

            BufferedImage toggleImage;
            if(options[i] == GameSettings.DARK_MODE) {
                toggleImage = isEnabled
                        ? (isHovered ? DARK_MODE_ON_HIGHLIGHTED : DARK_MODE_ON)
                        : (isHovered ? TOGGLE_OFF_HIGHLIGHTED : TOGGLE_OFF);
            } else if(options[i] == GameSettings.HARD_MODE) {
                toggleImage = isEnabled
                        ? (isHovered ? HARD_MODE_ON_HIGHLIGHTED : HARD_MODE_ON)
                        : (isHovered ? TOGGLE_OFF_HIGHLIGHTED : TOGGLE_OFF);
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
        g2.setColor(Colorblindness.filter(Colors.getBackground()));
        g2.fillRect(0, 0, getTotalWidth(), render.scale(RenderContext.BASE_HEIGHT));

        List<Achievement> list =
                boardService.getServiceFactory()
                        .getAchievementService()
                        .getSortedAchievements();
        int stroke = 4;
        int x = 32, y = 32;
        int arc = 40;

        GUIService.drawBox(g2, stroke, x, y,
                render.scale(RenderContext.BASE_WIDTH - x * 2),
                render.scale(RenderContext.BASE_HEIGHT - y * 2), arc, arc, true,
                false, 255);

        String text = ACHIEVEMENTS;
        int headerY = render.getOffsetY() + render.scale(OPTION_Y);
        int headerWidth = fm.stringWidth(text);
        g2.setFont(GUIService.getFont(GUIService.getMENU_FONT()));
        g2.setColor(Colorblindness.filter(Colors.getTheme() == Theme.DEFAULT
                ? Color.WHITE : Colors.getForeground()));
        g2.drawString(text, getCenterX(getTotalWidth(), headerWidth), headerY);

        int spacing = 25;
        int startY = headerY + spacing * 2;
        int width = RenderContext.BASE_WIDTH/2;
        int height = 100, arcWidth = 32, arcHeight = 32;
        x = getCenterX(getTotalWidth(), width);
        boolean hasBackground = true;

        int itemsPerPage = MovesManager.getITEMS_PER_PAGE();
        int start = (currentPage - 1) * itemsPerPage;
        int end = Math.min(start + itemsPerPage, list.size());

        BufferedImage img = null;
        for(int i = start; i < end; i++) {
            Achievement a = list.get(i);
            Rectangle hitbox = new Rectangle(
                    x, startY, width, height
            );

            boolean isHovered = hitbox.contains(mouse.getX(), mouse.getY());

            int textX = x + render.scale(110);
            int titleY = startY + render.scale(60);
            int descY = titleY;

            Theme currentTheme = Colors.getTheme();
            g2.setColor(Colorblindness.filter(Colors.getTheme() == Theme.DEFAULT
                    ? Color.WHITE : Colors.getForeground()));
            g2.setFont(GUIService.getFont(GUIService.getMENU_FONT()));

            if(isHovered) {
                GUIService.drawBox(g2, stroke, x, startY,
                        width, height, arcWidth, arcHeight, hasBackground,
                        true, 255);
                g2.drawString(a.getId().getDescription(), textX, descY);
            } else {
                GUIService.drawBox(g2, stroke, x, startY,
                        width, height, arcWidth, arcHeight, hasBackground,
                        false, 255);
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
        int stroke = 4;
        int x = 32, y = 32;
        int arc = 40;

        GUIService.drawBox(g2, stroke, x, y,
                render.scale(RenderContext.BASE_WIDTH - x * 2),
                render.scale(RenderContext.BASE_HEIGHT - y * 2), arc, arc, true,
                false, 255);

        g2.setFont(GUIService.getFont(GUIService.getMENU_FONT()));
        FontMetrics fm = g2.getFontMetrics();

        int headerY = render.getOffsetY() + render.scale(OPTION_Y);
        int headerWidth = fm.stringWidth(SAVES);
        g2.setColor(Colorblindness.filter(Color.WHITE));
        g2.drawString(SAVES, getCenterX(getTotalWidth(), headerWidth), headerY);

        if(saves.isEmpty()) {
            return;
        }

        int spacing = 25;
        int startY = headerY + spacing * 2;
        int width = RenderContext.BASE_WIDTH/2;
        int height = 100, arcWidth = 32, arcHeight = 32;
        x = getCenterX(getTotalWidth(), width);
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
                        true, 255);
            } else {
                GUIService.drawBox(g2, stroke, x, startY,
                        width, height, arcWidth, arcHeight, hasBackground,
                        false, 255);
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
        g2.setColor(Colorblindness.filter(Colors.getForeground()));
        String text = GameService.getState() == GameState.CHECKMATE ?
                CHECKMATE : STALEMATE;
        g2.drawString(text, getCenterX(getTotalWidth(), headerWidth),headerY);
    }

    public BufferedImage getSprite(int i) {
        return switch (i) {
            case 0 -> DARK_MODE_ON;
            case 1 -> DARK_MODE_ON_HIGHLIGHTED;
            case 2 -> TOGGLE_ON;
            case 3 -> TOGGLE_OFF;
            case 4 -> TOGGLE_ON_HIGHLIGHTED;
            case 5 -> TOGGLE_OFF_HIGHLIGHTED;
            case 6 -> HARD_MODE_ON;
            case 7 -> HARD_MODE_ON_HIGHLIGHTED;
            default -> null;
        };
    }
}
