package org.chess.service;

import org.chess.entities.*;
import org.chess.input.Mouse;
import org.chess.gui.Sound;
import org.chess.input.MoveManager;
import org.chess.render.BoardRender;
import org.chess.render.MenuRender;
import org.chess.render.MovesRender;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

public class GUIService {
    private static Font font;
    private static final int MENU_SPACING = 40;
    private static final int MENU_START_Y = 80;
    private static final int MENU_FONT = 32;
    private static final int EXTRA_WIDTH = 150;
    private static final int LEFT_PANEL_CENTER_X = EXTRA_WIDTH/2;
    private static final int GRAPHICS_OFFSET = EXTRA_WIDTH/2;
    private static final int MOVES_CAP = 14;
    private final int TIMER_Y;
    private static Color background;
    private static Color foreground;

    private final Sound fx;
    private final BoardRender boardRender;
    private final MenuRender menuRender;
    private final MovesRender moveRender;

    private static BufferedImage logo;
    private final BufferedImage YES;
    private final BufferedImage NO;

    private final PieceService pieceService;
    private final BoardService boardService;
    private final GameService gameService;
    private final ModelService modelService;
    private final TimerService timerService;
    private final Mouse mouse;
    private static PromotionService promotionService;

    public GUIService(PieceService pieceService, BoardService boardService,
                      GameService gameService,
                      PromotionService promotionService,
                      ModelService modelService,
                      MoveManager moveManager, TimerService timerService,
                      Mouse mouse) {
        this.pieceService = pieceService;
        this.boardService = boardService;
        this.gameService = gameService;
        this.modelService = modelService;
        this.timerService = timerService;
        this.mouse = mouse;
        this.fx = new Sound();
        this.boardRender = new BoardRender(this, pieceService, boardService, promotionService);
        this.menuRender  = new MenuRender(this, gameService, boardService,
                moveManager, mouse);
        this.moveRender  = new MovesRender(boardService, this);

        this.boardService.setPieces();
        GUIService.promotionService = promotionService;
        logo = null;

        try {
            logo = getImage("/ui/logo");
            YES = getImage("/ticks/tick_yes");
            NO = getImage("/ticks/tick_no");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            font = Font.createFont(Font.TRUETYPE_FONT,
                    Objects.requireNonNull(Board.class.getResourceAsStream(
                            "/ui/Monocraft.ttf")));
            GraphicsEnvironment ge =
                    GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(font);
        } catch(Exception e) {
            System.err.println(e.getMessage());
            font = new Font("Helvetica", Font.BOLD, 30);
        }
        TIMER_Y = 475;
    }

    public static int getWIDTH() {
        return EXTRA_WIDTH + Board.getSquare() * 8;
    }

    public static int getEXTRA_WIDTH() {
        return EXTRA_WIDTH;
    }

    public static int getHEIGHT() {
        return Board.getSquare() * 8;
    }

    public static Font getFont(int size) {
        return font.deriveFont(Font.PLAIN, (float) size);
    }

    public static Font getFontBold(int size) {
        return font.deriveFont(Font.BOLD, (float) size);
    }

    public static int getMENU_SPACING() {
        return MENU_SPACING;
    }

    public static int getMENU_START_Y() {
        return MENU_START_Y;
    }

    public static int getMENU_FONT() {
        return MENU_FONT;
    }

    public BufferedImage getYES() {
        return YES;
    }

    public BufferedImage getNO() {
        return NO;
    }

    public static Color getNewBackground() {
        return background;
    }

    public static Color getNewForeground() {
        return foreground;
    }

    public static void setNewBackground(Color color) {
        background = color;
    }

    public static void setNewForeground(Color color) {
        foreground = color;
    }

    public static BufferedImage getLogo() {
        return logo;
    }

    public static int getGRAPHICS_OFFSET() {
        return GRAPHICS_OFFSET;
    }

    public static int getMOVES_CAP() {
        return MOVES_CAP;
    }

    public BoardRender getBoardRender() {
        return boardRender;
    }

    public MenuRender getMenuRender() {
        return menuRender;
    }

    public MovesRender getMovesRender() {
        return moveRender;
    }

    public Sound getFx() {
        return fx;
    }

    public Mouse getMouse() {
        return mouse;
    }

    public BufferedImage getImage(String path) throws IOException {
        return ImageIO.read(Objects.requireNonNull(
                getClass().getResourceAsStream(path + ".png")));
    }

    public void drawTick(Graphics2D g2, boolean isLegal) {
        if(!BooleanService.canTick) { return; }
        FontMetrics fm = g2.getFontMetrics(GUIService.getFont(24));
        BufferedImage image = isLegal ? YES : NO;
        int size = Board.getSquare();
        int x = LEFT_PANEL_CENTER_X - size/2;
        int y = TIMER_Y - fm.getAscent() - size - 5;
        g2.drawImage(image, x, y, size, size, null);
    }

    public static Rectangle getHITBOX(int x, int y) {
        return new Rectangle(x, y, 200, 40);
    }

    public void drawTimer(Graphics2D g2) {
        g2.setFont(GUIService.getFont(24));
        g2.setColor(foreground);
        FontMetrics fm = g2.getFontMetrics();
        String time = timerService.getTimeString();
        int textWidth = fm.stringWidth(time);
        int x = LEFT_PANEL_CENTER_X - textWidth/2;
        g2.drawString(timerService.getTimeString(), x, TIMER_Y);
    }
}
