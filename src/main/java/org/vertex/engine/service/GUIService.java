package org.vertex.engine.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertex.engine.entities.Board;
import org.vertex.engine.enums.Theme;
import org.vertex.engine.gui.Colors;
import org.vertex.engine.manager.MovesManager;
import org.vertex.engine.render.Colorblindness;
import org.vertex.engine.render.RenderContext;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class GUIService {
    private static Font font;
    private static Font font_bold;
    private static BufferedImage logo;
    private static final int MENU_SPACING = 40;
    private static final int MENU_START_X = 100;
    private static final int MENU_START_Y = 160;
    private static final int MENU_FONT = 32;
    private static final int MENU_SUBFONT = 24;
    private static final int MOVES_CAP = 28;
    private static final int PADDING = 90;

    private final RenderContext render;

    private final transient BufferedImage YES;
    private final transient BufferedImage NO;

    private final PieceService pieceService;
    private final BoardService boardService;
    private final GameService gameService;
    private final ModelService modelService;
    private final TimerService timerService;
    private static PromotionService promotionService;

    private static final Logger log = LoggerFactory.getLogger(GUIService.class);

    public GUIService(RenderContext render, PieceService pieceService,
                      BoardService boardService,
                      GameService gameService,
                      PromotionService promotionService,
                      ModelService modelService,
                      MovesManager movesManager, TimerService timerService) {
        this.render = render;
        this.pieceService = pieceService;
        this.boardService = boardService;
        this.gameService = gameService;
        this.modelService = modelService;
        this.timerService = timerService;
        GUIService.promotionService = promotionService;
        logo = null;
        try {
            YES = getImage("/ticks/tick_yes");
            NO = getImage("/ticks/tick_no");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            font = Font.createFont(Font.TRUETYPE_FONT,
                    Objects.requireNonNull(Board.class.getResourceAsStream(
                            "/fonts/Monocraft.ttf")));
            font_bold = Font.createFont(Font.TRUETYPE_FONT,
                    Objects.requireNonNull(Board.class.getResourceAsStream(
                            "/fonts/Monocraft-Semibold.ttf")));
            GraphicsEnvironment ge =
                    GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(font_bold);
            ge.registerFont(font);
        } catch(Exception e) {
            log.error(e.getMessage());
            font = new Font("Helvetica", Font.BOLD, 30);
        }
    }

    public static Font getFont(int size) {
        return font.deriveFont(Font.PLAIN, (float) size);
    }

    public static Font getFontBold(int size) {
        return font_bold.deriveFont(Font.PLAIN, (float) size);
    }

    public static int getMENU_SPACING() {
        return MENU_SPACING;
    }

    public static int getMENU_START_Y() {
        return MENU_START_Y;
    }

    public static int getMENU_START_X() {
        return MENU_START_X;
    }

    public static int getMENU_FONT() {
        return MENU_FONT;
    }

    public static int getMENU_SUBFONT() {
        return MENU_SUBFONT;
    }

    public BufferedImage getYES() {
        return YES;
    }

    public BufferedImage getNO() {
        return NO;
    }

    public static BufferedImage getLogo() {
        try {
            return switch(Colors.getTheme()) {
                case DEFAULT -> logo = getImage("/ui/logo/logo_final_v2");
                case BLACK -> logo = getImage("/ui/logo/logo_final_v3");
                case LEGACY -> logo = getImage("/ui/logo/logo_final_v3_creme");
                case OCEAN -> logo = getImage("/ui/logo/logo_final_v3_ocean");
                case FOREST -> logo = getImage("/ui/logo/logo_final_v3_forest");
                case LOGO -> logo = getImage("/ui/logo/logo_final_v3_legacy-logo");
                case FAIRY -> logo = getImage("/ui/logo/logo_final_v3_fairy");
            };
        } catch (RuntimeException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static int getMOVES_CAP() {
        return MOVES_CAP;
    }

    public static int getPADDING() {
        return PADDING;
    }

    public static BufferedImage getImage(String path) throws IOException {
        InputStream stream = GUIService.class.getResourceAsStream(path + ".png");
        if (stream == null) {
            log.error("Resource not found: {}.png", path);
            return null;
        }
        return ImageIO.read(stream);
    }

    public void drawTimer(Graphics2D g2) {
        Color filtered = Colorblindness.filter(Theme.DEFAULT.getBackground());

        int boardX = render.getBoardRender().getBoardOriginX();
        int boardY = render.getBoardRender().getBoardOriginY();
        int boardWidth = Board.getSquare() * boardService.getBoard().getCOL();

        g2.setFont(getFont(MENU_FONT));
        FontMetrics fm = g2.getFontMetrics();
        String time = timerService.getTimeString();
        int textWidth = fm.stringWidth(time);
        int textHeight = fm.getAscent() + fm.getDescent();

        int innerPadding = render.scale(30);
        int padding = render.scale(PADDING);

        int textX = boardX + (boardWidth - textWidth)/2;
        int textY = boardY - padding - fm.getDescent();

        int boxX = textX - innerPadding;
        int boxY = textY - fm.getAscent() - innerPadding;
        int boxWidth = textWidth + 2 * innerPadding;
        int boxHeight = textHeight + 2 * innerPadding;

        drawBox(g2, 4, boxX, boxY, boxWidth,
                boxHeight, 32, 32, true, false, 255);
        g2.setColor(filtered);
        g2.drawString(time, textX, textY);
    }

    public void drawTick(Graphics2D g2, boolean isLegal) {
        if(!BooleanService.canShowTick) { return; }
        if(pieceService.getHeldPiece() == null) return;

        BufferedImage image = isLegal ? YES : NO;
        image = Colorblindness.filter(image);

        int size = render.scale(Board.getSquare());
        int boardX = render.getBoardRender().getBoardOriginX();
        int boardY = render.getBoardRender().getBoardOriginY();
        int boardWidth = Board.getSquare() * boardService.getBoard().getCOL();

        int padding = render.scale(PADDING + 30);
        int tickX = boardX + (boardWidth - size)/2;
        int tickY = boardY - size - padding;

        g2.drawImage(image, tickX, tickY, size, size, null);
    }

    public static void drawBox(Graphics2D g2, int stroke, int x, int y, int width,
                               int height, int arcWidth, int arcHeight,
                               boolean hasBackground, boolean isHighlighted,
                               int alpha) {
        if(hasBackground) {
            g2.setColor(Colorblindness.filter(Colors.SETTINGS));
            g2.fillRoundRect(x, y, width, height, arcWidth, arcHeight);
        }

        if(isHighlighted) {
            g2.setColor(Colorblindness.filter(Colors.getHighlight()));
        } else {
            g2.setColor(Colorblindness.filter(Colors.getEdge()));
        }

        g2.setStroke(new BasicStroke(stroke));
        g2.drawRoundRect(x, y, width, height, arcWidth, arcHeight);
    }

    public static Rectangle getHITBOX(int x, int y, int width, int height) {
        return new Rectangle(x, y, width, height);
    }
}