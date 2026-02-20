package org.vertex.engine.render;

import org.vertex.engine.entities.Achievement;
import org.vertex.engine.entities.Board;
import org.vertex.engine.entities.Piece;
import org.vertex.engine.enums.*;
import org.vertex.engine.gui.Colors;
import org.vertex.engine.input.Keyboard;
import org.vertex.engine.input.KeyboardInput;
import org.vertex.engine.input.Mouse;
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
    private static final int ARC = 32;
    private static final int STROKE = 6;
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
    private UIService uiService;
    private KeyboardInput keyUI;
    private AnimationService animationService;
    private AchievementService achievementService;
    private PieceService pieceService;
    private PromotionService promotionService;
    private Mouse mouse;
    private AchievementSprites sprites;

    public MenuRender(RenderContext render) {
        MenuRender.render = render;
        cb = ColorblindType.PROTANOPIA;
    }

    public void init() {
        this.sprites = new AchievementSprites();
        try {
            TOGGLE_ON = UIService.getImage("/ui/toggle_on");
            TOGGLE_OFF = UIService.getImage("/ui/toggle_off");
            TOGGLE_ON_HIGHLIGHTED = UIService.getImage("/ui/toggle_onh");
            TOGGLE_OFF_HIGHLIGHTED = UIService.getImage("/ui/toggle_offh");
            DARK_MODE_ON = UIService.getImage("/ui/dark-mode_on");
            DARK_MODE_ON_HIGHLIGHTED = UIService.getImage("/ui/dark-mode_onh");
            HARD_MODE_ON = UIService.getImage("/ui/hardmode_on");
            HARD_MODE_ON_HIGHLIGHTED = UIService.getImage("/ui/hardmode_onh");

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

    public UIService getGuiService() {
        return uiService;
    }

    public void setUIService(UIService UIService) {
        this.uiService = UIService;
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

    public PromotionService getPromotionService() {
        return promotionService;
    }

    public void setPromotionService(PromotionService promotionService) {
        this.promotionService = promotionService;
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

    public Mouse getMouse() {
        return mouse;
    }

    public void setMouse(Mouse mouse) {
        this.mouse = mouse;
    }

    public PieceService getPieceService() {
        return pieceService;
    }

    public void setPieceService(PieceService pieceService) {
        this.pieceService = pieceService;
    }

    public static int getARC() {
        return ARC;
    }

    private static void drawLogo(Graphics2D g2, int containerWidth) {
        if(UIService.getLogo() == null) { return; }
        BufferedImage img = UIService.getLogo();
        int logoWidth = 0;
        int logoHeight = 0;
        img = Colorblindness.filter(img);
        int boardWidth = Board.getSquare() * 8;
        logoWidth = (int) (UIService.getLogo().getWidth()/SCALE);
        logoHeight = (int) (UIService.getLogo().getHeight()/SCALE);
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

        Font baseFont = UIService.getFont(UIService.getMENU_FONT());
        Font selectedFont = UIService.getFontBold(UIService.getMENU_FONT());

        drawLogo(g2, getTotalWidth());

        int centerY = 800;
        int spacing = render.scale(UIService.getMENU_SPACING());
        int centerX = render.getOffsetX() + getTotalWidth() / 2;
        int totalWidth = 0;

        for(int i = 0; i < options.length; i++) {
            String text = options[i].getLabel();
            if(text.equals(GameMenu.PLAY.getLabel())) { text += GameService.getGame().getLabel(); }
            boolean isSelected = i == keyUI.getSelectedIndexY();
            g2.setFont(isSelected ? selectedFont : baseFont);
            FontMetrics metrics = g2.getFontMetrics();
            totalWidth += metrics.stringWidth(text);
            if(i < options.length - 1) {
                totalWidth += spacing;
            }
        }

        int startX = centerX - totalWidth/2;
        int currentX = startX;

        GameMenu hoveredOption = null;
        Rectangle hoveredHitbox = null;
        String tooltip = "";

        for(int i = 0; i < options.length; i++) {
            GameMenu op = options[i];
            String option = op.getLabel();
            if(op == GameMenu.PLAY) { option += GameService.getGame().getLabel(); }
            boolean isSelected = i == keyUI.getSelectedIndexY();
            g2.setFont(isSelected ? selectedFont : baseFont);
            FontMetrics metrics = g2.getFontMetrics();
            int textWidth = metrics.stringWidth(option);
            int textHeight = metrics.getHeight();
            int ascent = metrics.getAscent();
            int paddingX = 25;
            int paddingY = 25;
            int buttonWidth = textWidth + paddingX;
            int buttonHeight = textHeight + paddingY;
            Color textColor = isSelected ?
                    Colorblindness.filter(Colors.getHighlight())
                    : Colorblindness.filter(Colors.getBackground());

            Rectangle hitbox = new Rectangle(currentX - 2, centerY,
                    buttonWidth, buttonHeight);

            uiService.drawButton(g2, currentX - 2, centerY,
                    buttonWidth, buttonHeight, ARC, isSelected);

            int textX = currentX + (buttonWidth - textWidth) / 2;
            int textY = centerY + (buttonHeight - textHeight) / 2 + ascent;
            g2.setColor(textColor);
            g2.drawString(option, textX, textY);

            if(hitbox.contains(mouse.getX(), mouse.getY())) {
                hoveredOption = op;
                hoveredHitbox = hitbox;
            }
            currentX += buttonWidth + spacing;
        }

        if(hoveredOption != null) {
            if(hoveredOption == GameMenu.PLAY) {
                tooltip = gameService.getTooltip(GameService.getGame(),
                        gameService.getSaveManager().autosaveExists());
            } else {
                tooltip = hoveredOption.getTooltip();
            }
            uiService.drawTooltip(g2, tooltip, 16, ARC/2);
        }
    }

    public void drawOptionsMenu(Graphics2D g2, GameSettings[] options) {
        g2.setColor(Colorblindness.filter(Colors.getBackground()));
        g2.fillRect(0, 0, getTotalWidth(), render.scale(RenderContext.BASE_HEIGHT));

        int x = 32, y = 32;

        UIService.drawBox(g2, STROKE, x, y,
                render.scale(RenderContext.BASE_WIDTH - x * 2),
                render.scale(RenderContext.BASE_HEIGHT - y * 2), ARC, true,
                false, 255);

        g2.setFont(UIService.getFont(UIService.getMENU_FONT()));

        String header = SETTINGS;
        int headerY = render.getOffsetY() + render.scale(OPTION_Y);
        int headerWidth = g2.getFontMetrics().stringWidth(header);
        g2.setColor(Colorblindness.filter(Colors.getTheme() == Theme.DEFAULT
                ? Color.WHITE : Colors.getForeground()));
        g2.drawString(header, getCenterX(getTotalWidth(), headerWidth),headerY);

        int startY = headerY + render.scale(100);
        int lineHeight = g2.getFontMetrics().getHeight() + render.scale(10);
        int itemsPerPage = 6;

        int startIndex = keyUI.getCurrentPage() * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, options.length);

        int gap = render.scale(100);
        int maxRowWidth = 0;
        g2.setFont(UIService.getFont(UIService.getMENU_FONT()));

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
            uiService.drawToggle(g2, toggleImage, render.getOffsetX() + toggleX,
                    render.getOffsetY() + toggleY, toggleWidth, toggleHeight);

            startY += lineHeight;
        }
    }

    public void drawAchievementsMenu(Graphics2D g2) {
        g2.setColor(Colorblindness.filter(Colors.getBackground()));
        g2.fillRect(0, 0, getTotalWidth(), render.scale(RenderContext.BASE_HEIGHT));

        List<Achievement> list = achievementService.init();
        int x = 32, y = 32;

        UIService.drawBox(g2, STROKE, x, y,
                render.scale(RenderContext.BASE_WIDTH - x * 2),
                render.scale(RenderContext.BASE_HEIGHT - y * 2), ARC, true,
                false, 255);

        String text = ACHIEVEMENTS;
        int headerY = render.getOffsetY() + render.scale(OPTION_Y);
        int headerWidth = g2.getFontMetrics().stringWidth(text);
        g2.setFont(UIService.getFont(UIService.getMENU_FONT()));
        g2.setColor(Colorblindness.filter(Colors.getTheme() == Theme.DEFAULT
                ? Color.WHITE : Colors.getForeground()));
        g2.drawString(text, getCenterX(getTotalWidth() - 150, headerWidth),
                headerY);

        int spacing = 25;
        int startY = headerY + spacing * 2;
        int width = RenderContext.BASE_WIDTH/2;
        int height = 100;
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
            g2.setFont(UIService.getFont(UIService.getMENU_FONT()));

            if(isSelected) {
                UIService.drawBox(g2, STROKE, x, startY,
                        width, height, ARC, hasBackground,
                        true, 255);
                g2.drawString(a.getId().getDescription(), textX, descY);
            } else {
                UIService.drawBox(g2, STROKE, x, startY,
                        width, height, ARC, hasBackground,
                        false, 255);
                g2.drawString(a.getId().getTitle(), textX, titleY);
            }

            img = AchievementSprites.getSprite(a);
            if(img != null && !a.isUnlocked()) {
                img = AchievementLock.filter(img, a.isUnlocked());
            }

            if(img != null) {
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

            UIService.drawBox(g2, STROKE,
                    zoomX, zoomY, zoomWidth, zoomHeight,
                    ARC, true, false,
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

    public void drawPromotions(Graphics2D g2) {
        Piece hp = pieceService.getHoveredPiece();
        if(hp == null || !hp.isPromoted()) { return; }

        List<Piece> promotionOptions = promotionService.getPromotions(hp);

        int squareSize = render.scale(Board.getSquare());
        int menuWidth = promotionOptions.size() * squareSize;
        int menuHeight = squareSize;

        int screenWidth = RenderContext.BASE_WIDTH;
        int screenHeight = RenderContext.BASE_HEIGHT;

        int x = (screenWidth - menuWidth)/2;
        int y = (screenHeight - menuHeight)/2;

        UIService.drawBox(g2, STROKE, x, y, menuWidth, menuHeight,
                ARC, true, false, 180);

        for(int i = 0; i < promotionOptions.size(); i++) {
            Piece p = promotionOptions.get(i);
            int optionX = x + i * squareSize;
            BufferedImage sprite = pieceService.getSprite(p);
            g2.drawImage(sprite, x, y, squareSize, squareSize, null);
        }
    }

    public void drawSandboxMenu(Graphics2D g2) {
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

    public void drawCheckmate(Graphics2D g2) {
        if(gameService.getState() != GameState.CHECKMATE) { return; }
        g2.setFont(UIService.getFontBold(UIService.getMENU_FONT()));
        FontMetrics fm = g2.getFontMetrics();

        int headerY = render.getOffsetY() + render.scale(200);
        int headerWidth = fm.stringWidth(CHECKMATE);
        g2.setColor(Colorblindness.filter(Colors.getForeground()));
        String text = gameService.getState() == GameState.CHECKMATE ?
                CHECKMATE : STALEMATE;
        g2.drawString(text, getCenterX(getTotalWidth(), headerWidth),headerY);
    }

    public void showTooltip(Graphics2D g2) {
        Piece p = pieceService.getHoveredPiece();

        final int COL = boardService.getBoard().getCol();
        final int SQUARE = render.scale(Board.getSquare());

        int boardSize = SQUARE * COL;
        int boardX = (RenderContext.BASE_WIDTH - boardSize)/2;
        int boardY = (RenderContext.BASE_HEIGHT - boardSize)/2;
        int mouseBoardX = render.unscaleX(mouse.getX()) - boardX;
        int mouseBoardY = render.unscaleY(mouse.getY()) - boardY;
        int mouseCol = mouseBoardX/Board.getSquare();
        int mouseRow = mouseBoardY/Board.getSquare();

        Piece hovered = PieceService.getPieceAt(mouseCol, mouseRow, pieceService.getPieces());
        pieceService.setHoveredPiece(hovered);

        if(p != null) {
            if(mouseCol == p.getCol() && mouseRow == p.getRow()) {
                TypeID id = p.getTypeID();
                TypeID shogiID = p.getShogiID();
                if(shogiID != null && GameService.getGame() == Games.SHOGI) {
                    id = shogiID;
                }
                int padding = 16;
                String square = boardService.getSquareNameAt(p.getCol(), p.getRow());
                String text = id.name() + " " + square.toUpperCase();
                g2.setFont(UIService.getFont(UIService.getMENU_FONT()));
                uiService.drawTooltip(g2, text, padding, ARC);
            }
        }
    }
}