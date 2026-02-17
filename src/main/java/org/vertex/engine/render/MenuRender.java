package org.vertex.engine.render;

import org.vertex.engine.entities.Achievement;
import org.vertex.engine.entities.Board;
import org.vertex.engine.enums.*;
import org.vertex.engine.gui.Colors;
import org.vertex.engine.input.Keyboard;
import org.vertex.engine.input.KeyboardInput;
import org.vertex.engine.manager.MovesManager;
import org.vertex.engine.service.*;

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

    private static RenderContext render;
    private GameService gameService;
    private BoardService boardService;
    private MovesManager movesManager;
    private GUIService guiService;
    private KeyboardInput keyUI;
    private AnimationService animationService;
    private AchievementService achievementService;
    private AchievementSprites sprites;

    public MenuRender(RenderContext render) {
        MenuRender.render = render;
        cb = ColorblindType.PROTANOPIA;
    }

    public void init() {
        this.sprites = new AchievementSprites();
        try {
            TOGGLE_ON = GUIService.getImage("/ui/toggle_on");
            TOGGLE_OFF = GUIService.getImage("/ui/toggle_off");
            TOGGLE_ON_HIGHLIGHTED = GUIService.getImage("/ui/toggle_onh");
            TOGGLE_OFF_HIGHLIGHTED = GUIService.getImage("/ui/toggle_offh");
            DARK_MODE_ON = GUIService.getImage("/ui/dark-mode_on");
            DARK_MODE_ON_HIGHLIGHTED = GUIService.getImage("/ui/dark-mode_onh");
            HARD_MODE_ON = GUIService.getImage("/ui/hardmode_on");
            HARD_MODE_ON_HIGHLIGHTED = GUIService.getImage("/ui/hardmode_onh");

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

    public KeyboardInput getKeyUI() {
        return keyUI;
    }

    public void setKeyUI(KeyboardInput keyUI) {
        this.keyUI = keyUI;
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

    public AnimationService getAnimationService() {
        return animationService;
    }

    public void setAnimationService(AnimationService animationService) {
        this.animationService = animationService;
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

    private int getCenterY(int totalHeight, int objectHeight) {
        return render.getOffsetY() + (totalHeight - objectHeight) / 2;
    }

    public int getTotalWidth() {
        return totalWidth = render.scale(RenderContext.BASE_WIDTH);
    }

    public static int getOPTION_X() {
        return OPTION_X;
    }

    public static int getOPTION_Y() {
        return OPTION_Y;
    }

    public AchievementService getAchievementService() {
        return achievementService;
    }

    public void setAchievementService(AchievementService achievementService) {
        this.achievementService = achievementService;
    }

    private void drawToggle(Graphics2D g2, BufferedImage image, int x, int y,
                            int width, int height) {
        g2.drawImage(image, x, y, width, height, null);
    }

    private static void drawLogo(Graphics2D g2, int containerWidth) {
        if(GUIService.getLogo() == null) { return; }
        BufferedImage img = GUIService.getLogo();
        int logoWidth = 0;
        int logoHeight = 0;
        img = Colorblindness.filter(img);
        int boardWidth = Board.getSquare() * 8;
        logoWidth = (int) (GUIService.getLogo().getWidth()/SCALE);
        logoHeight = (int) (GUIService.getLogo().getHeight()/SCALE);
        int boardCenterX = render.getOffsetX() + render.scale(
                RenderContext.BASE_WIDTH) * 2 + boardWidth/2;
        int x = getCenterX(containerWidth, logoWidth);
        int y =
                render.getOffsetY() + render.scale(RenderContext.BASE_HEIGHT)/4;
        g2.drawImage(img, x, y, logoWidth, logoHeight, null);
    }

    public void drawGraphics(Graphics2D g2, GameMenu[] options) {
        g2.setColor(Colorblindness.filter(Colors.getBackground()));
        g2.fillRect(0, 0, getTotalWidth(), render.scale(RenderContext.BASE_HEIGHT));

        Font baseFont = GUIService.getFont(GUIService.getMENU_FONT());
        Font selectedFont = GUIService.getFontBold(GUIService.getMENU_FONT());

        drawLogo(g2, getTotalWidth());

        int centerY = 800;
        int spacing = render.scale(GUIService.getMENU_SPACING());
        int centerX = render.getOffsetX() + getTotalWidth() / 2;
        int totalWidth = 0;

        for (int i = 0; i < options.length; i++) {
            String text = options[i].getLabel();
            boolean isSelected = i == keyUI.getSelectedIndexY();
            g2.setFont(isSelected ? selectedFont : baseFont);
            FontMetrics metrics = g2.getFontMetrics();
            totalWidth += metrics.stringWidth(text);
            if (i < options.length - 1) {
                totalWidth += spacing;
            }
        }

        int startX = centerX - totalWidth / 2;
        int currentX = startX;

        for (int i = 0; i < options.length; i++) {
            GameMenu op = options[i];
            String option = op.getLabel();
            boolean isSelected = i == keyUI.getSelectedIndexY();
            g2.setFont(isSelected ? selectedFont : baseFont);
            FontMetrics metrics = g2.getFontMetrics();
            int textWidth = metrics.stringWidth(option);
            Color foreground = Colorblindness.filter(Colors.getForeground());
            Color textColor = isSelected ? Colors.getHighlight() : foreground;
            g2.setColor(textColor);
            g2.drawString(option, currentX, centerY);
            currentX += textWidth + spacing;
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

        String header = SETTINGS;
        int headerY = render.getOffsetY() + render.scale(OPTION_Y);
        int headerWidth = g2.getFontMetrics().stringWidth(header);
        g2.setColor(Colorblindness.filter(Colors.getTheme() == Theme.DEFAULT
                ? Color.WHITE : Colors.getForeground()));
        g2.drawString(header, getCenterX(getTotalWidth(), headerWidth),headerY);

        int startY = headerY + render.scale(90);
        int lineHeight = g2.getFontMetrics().getHeight() + render.scale(10);
        int itemsPerPage = 8;

        int startIndex = keyUI.getCurrentPage() * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, options.length);

        int gap = render.scale(100);
        int maxRowWidth = 0;
        g2.setFont(GUIService.getFont(GUIService.getMENU_FONT()));

        for(int i = startIndex; i < endIndex; i++) {
            GameSettings option = options[i];
            String enabledOption = ENABLE + option.getLabel();
            int textWidth = g2.getFontMetrics().stringWidth(enabledOption);
            int toggleWidth = render.scale(TOGGLE_ON.getWidth()/2);
            int rowWidth = textWidth + gap + toggleWidth;
            if(rowWidth > maxRowWidth) maxRowWidth = rowWidth;
        }

        for(int i = startIndex; i < endIndex; i++) {
            GameSettings option = options[i];
            int relativeIndex = i - startIndex;
            boolean isSelected = relativeIndex == keyUI.getSelectedIndexY();

            String enabledOption = ENABLE + option.getLabel();
            int textWidth = g2.getFontMetrics().stringWidth(enabledOption);
            int toggleWidth = render.scale(TOGGLE_ON.getWidth()/2);
            int toggleHeight = render.scale(TOGGLE_ON.getHeight()/2);

            int blockX = getCenterX(getTotalWidth(), maxRowWidth);
            int textX = blockX;
            int toggleX = blockX + maxRowWidth - toggleWidth;
            int toggleY = startY - toggleHeight;

            boolean isEnabled = option.get();

            g2.drawString(enabledOption, textX,
                    render.getOffsetY() + startY);

            BufferedImage toggleImage;
            if(options[i] == GameSettings.DARK_MODE) {
                toggleImage = isEnabled
                        ? (isSelected ? DARK_MODE_ON_HIGHLIGHTED : DARK_MODE_ON)
                        : (isSelected ? TOGGLE_OFF_HIGHLIGHTED : TOGGLE_OFF);
            } else if(options[i] == GameSettings.HARD_MODE) {
                toggleImage = isEnabled
                        ? (isSelected ? HARD_MODE_ON_HIGHLIGHTED : HARD_MODE_ON)
                        : (isSelected ? TOGGLE_OFF_HIGHLIGHTED : TOGGLE_OFF);
            } else {
                toggleImage = isEnabled
                        ? (isSelected ? TOGGLE_ON_HIGHLIGHTED : TOGGLE_ON)
                        : (isSelected ? TOGGLE_OFF_HIGHLIGHTED : TOGGLE_OFF);
            }
            drawToggle(g2, toggleImage, render.getOffsetX() + toggleX,
                    render.getOffsetY() + toggleY, toggleWidth, toggleHeight);

            startY += lineHeight;
        }
    }

    public void drawAchievementsMenu(Graphics2D g2) {
        g2.setColor(Colorblindness.filter(Colors.getBackground()));
        g2.fillRect(0, 0, getTotalWidth(), render.scale(RenderContext.BASE_HEIGHT));

        List<Achievement> list = achievementService.init();
        int stroke = 4;
        int x = 32, y = 32;
        int arc = 40;

        GUIService.drawBox(g2, stroke, x, y,
                render.scale(RenderContext.BASE_WIDTH - x * 2),
                render.scale(RenderContext.BASE_HEIGHT - y * 2), arc, arc, true,
                false, 255);

        String text = ACHIEVEMENTS;
        int headerY = render.getOffsetY() + render.scale(OPTION_Y);
        int headerWidth = g2.getFontMetrics().stringWidth(text);
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

        int itemsPerPage = KeyboardInput.getITEMS_PER_PAGE();
        int start = keyUI.getCurrentPage() * itemsPerPage;
        int end = Math.min(start + itemsPerPage, list.size());

        BufferedImage img = null;
        for(int i = start; i < end; i++) {
            Achievement a = list.get(i);
            int relativeIndex = i - start;
            boolean isSelected = relativeIndex == keyUI.getSelectedIndexY();

            int textX = x + render.scale(110);
            int titleY = startY + render.scale(60);
            int descY = titleY;

            Theme currentTheme = Colors.getTheme();
            g2.setColor(Colorblindness.filter(Colors.getTheme() == Theme.DEFAULT
                    ? Color.WHITE : Colors.getForeground()));
            g2.setFont(GUIService.getFont(GUIService.getMENU_FONT()));

            if(isSelected) {
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

        if(BooleanService.canZoomIn) {
            int zoomWidth  = render.scale(RenderContext.BASE_WIDTH/2);
            int zoomHeight = render.scale(RenderContext.BASE_HEIGHT/2);
            int zoomX = getCenterX(getTotalWidth(), zoomWidth);
            int zoomY = getCenterY(render.scale(RenderContext.BASE_HEIGHT), zoomHeight);

            GUIService.drawBox(g2, stroke,
                    zoomX,
                    zoomY,
                    zoomWidth,
                    zoomHeight,
                    arcWidth,
                    arcHeight,
                    true,
                    false,
                    180);

            int selectedIndex = keyUI.getSelectedIndexY();
            int actualIndex = start + selectedIndex;

            if(actualIndex >= 0 && actualIndex < list.size()) {
                Achievement selected = list.get(actualIndex);
                BufferedImage zoomImg = AchievementSprites.getSprite(selected);

                if(zoomImg != null) {
                    int padding = render.scale(40);

                    int imgSize = Math.min(
                            zoomWidth - padding * 2,
                            zoomHeight - padding * 2
                    );

                    int imgX = zoomX + (zoomWidth - imgSize) / 2;
                    int imgY = zoomY + (zoomHeight - imgSize) / 2;

                    g2.drawImage(zoomImg, imgX, imgY, imgSize, imgSize, null);
                }
            }
        }

    }

    public void drawSandboxMenu(Graphics2D g2) {
        if(!BooleanService.isSandboxEnabled) { return; }
        int stroke = 6, arc = 32;
        int boardX = render.getBoardRender().getBoardOriginX();
        int boardY = render.getBoardRender().getBoardOriginY();
        int boardWidth = Board.getSquare() * boardService.getBoard().getCOL();
        int boardHeight = Board.getSquare() * boardService.getBoard().getROW();
        int boardBottom = boardY + boardHeight;

        g2.setFont(GUIService.getFont(GUIService.getMENU_FONT()));
        FontMetrics fm = g2.getFontMetrics();
        Keyboard keyboard = boardService.getServiceFactory().getKeyboard();
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

        GUIService.drawBox(g2, stroke, boxX, boxY, boxWidth,
                boxHeight, arc, arc, true, false, 255);

        g2.setColor(Colorblindness.filter(Colors.getForeground()));
        g2.drawString(input, textX, textY);
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