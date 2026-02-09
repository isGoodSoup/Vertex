package org.chess.service;

import org.chess.entities.*;
import org.chess.enums.Tint;
import org.chess.enums.Type;
import org.chess.gui.Mouse;
import org.chess.gui.Sound;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

public class GUIService {
    private static Font font;
    public static final String[] optionsMenu = { "PLAY AGAINST", "EXIT" };
    public static final String[] optionsMode = { "PLAYER", "AI", "CHAOS" };
    private static final int MENU_SPACING = 40;
    private static final int MENU_START_Y = 80;
    private static final int MENU_FONT = 32;
    private int lastHoveredIndex = -1;
    private static Color background;
    private static Color foreground;
    private final Sound fx;

    private static BufferedImage logo;
    private final BufferedImage yes;
    private final BufferedImage no;

    private final PieceService pieceService;
    private final BoardService boardService;
    private final GameService gameService;
    private final Mouse mouse;
    private static PromotionService promotionService;

    public GUIService(PieceService pieceService, BoardService boardService,
                      GameService gameService, Mouse mouse) {
        this.pieceService = pieceService;
        this.boardService = boardService;
        this.gameService = gameService;
        this.mouse = mouse;
        this.fx = new Sound();
        boardService.setPieces();
        logo = null;

        try {
            logo = getImage("/ui/logo");
            yes = getImage("/ticks/tick_yes");
            no = getImage("/ticks/tick_no");
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
    }

    public static int getWIDTH() {
        return Board.getSquare() * 8;
    }

    public static int getHEIGHT() {
        return Board.getSquare() * 8;
    }

    public static Font getFont(int size) {
        return font.deriveFont(Font.PLAIN, (float) size);
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

    public static Color getNewBackground() {
        return background;
    }

    public static Color getNewForeground() {
        return foreground;
    }

    public BufferedImage getImage(String path) throws IOException {
        return ImageIO.read(Objects.requireNonNull(
                getClass().getResourceAsStream(path + ".png")));
    }

    public static void drawRandomBackground(boolean isColor) {
        background = isColor ? Board.getEven() : Board.getOdd();
        foreground = isColor ? Board.getOdd() : Board.getEven();
    }

    private static void drawLogo(Graphics2D g2) {
        g2.drawImage(logo,getWIDTH()/3 + 5,getHEIGHT()/7,
                logo.getWidth()/3,logo.getHeight()/3, null);
    }

    public void drawGraphics(Graphics2D g2, String[] options) {
        g2.setColor(getNewBackground());
        g2.fillRect(0, 0, getWIDTH(), getHEIGHT());
        g2.setFont(getFont(getMENU_FONT()));
        g2.setColor(getNewForeground());
        drawLogo(g2);

        int startY = getHEIGHT()/2 + getMENU_START_Y();
        int spacing = getMENU_SPACING();

        for(int i = 0; i < options.length; i++) {
            int textWidth = g2.getFontMetrics().stringWidth(options[i]);
            int x = (getWIDTH() - textWidth)/2;
            int y = startY + i * spacing;
            boolean isHovered = getHitbox(y).contains(mouse.getX(),
                    mouse.getY());
            g2.setColor(isHovered ? Color.WHITE : getNewForeground());
            g2.drawString(options[i], x, y);

            if (isHovered && lastHoveredIndex != i) {
                fx.play(BooleanService.getRandom(1, 2));
                lastHoveredIndex = i;
            }
        }
    }

    private void drawTick(Graphics2D g2, boolean isLegal) {
        Piece currentPiece = PieceService.getPiece();
        double scale = PieceService.getPiece().getScale();
        int size = (int) (Board.getSquare() * scale);
        int x = currentPiece.getX() - size / 2;
        int y = currentPiece.getY() - size / 2;
        BufferedImage image = isLegal ? yes : no;
        g2.drawImage(image, x, y, size, size, null);
    }

    public void drawBoard(Graphics2D g2) {
        Piece currentPiece = PieceService.getPiece();
        drawBaseBoard(g2);
        Piece hovered = pieceService.getHoveredPiece();
        g2.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
        );

        for (Piece p : PieceService.getPieces()) {
            if (p != currentPiece) {
                if (p == hovered) {
                    p.setScale(p.getDEFAULT_SCALE() + p.getMORE_SCALE()/2);
                } else {
                    p.setScale(p.getDEFAULT_SCALE());
                }
                drawPiece(g2, p);
            }
        }

        if(currentPiece != null) {
            drawPiece(g2, currentPiece);
        }

        if(currentPiece != null && BooleanService.isDragging) {
            drawTick(g2, BooleanService.isLegal);
        }
        drawPromotions(g2);
    }

    public void drawBaseBoard(Graphics2D g2) {
        final int ROW = boardService.getBoard().getROW();
        final int COL = boardService.getBoard().getCOL();
        final Color EVEN = Board.getEven();
        final Color ODD = Board.getOdd();
        final int PADDING = Board.getPadding();
        final int SQUARE = Board.getSquare();

        String[] letters = {"A","B","C","D","E","F","G","H"};
        for(int row = 0; row < ROW; row++) {
            for(int col = 0; col < COL; col++) {
                boolean isEven = (row + col) % 2 == 0;
                g2.setColor(isEven ? EVEN : ODD);
                g2.fillRect(col * SQUARE, row * SQUARE, SQUARE, SQUARE);
                g2.setFont(GUIService.getFont(14));
                g2.setColor(isEven ? ODD : EVEN);
                g2.drawString(BoardService.getSquareName(col, row),
                        col * SQUARE + PADDING,
                        row * SQUARE + SQUARE - PADDING);
            }
        }
    }

    public void drawPromotions(Graphics2D g2) {
        if(!BooleanService.isPromotionPending) { return; }

        int size = Board.getSquare();
        int totalWidth = size * 4;
        int startX = (getWIDTH() - totalWidth) / 2;
        int startY = (getHEIGHT() - size) / 2;

        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRect(0, 0, getWIDTH(), getHEIGHT());

        Type[] options = { Type.QUEEN, Type.ROOK, Type.BISHOP, Type.KNIGHT };

        int hoverIndex = -1;
        for(int i = 0; i < options.length; i++) {
            int x0 = startX + i * size;
            int x1 = x0 + size;
            int y1 = startY + size;

            if(mouse.getX() >= x0 && mouse.getX() <= x1 &&
                    mouse.getY() >= startY && mouse.getY() <= y1) {
                hoverIndex = i;
                break;
            }
        }

        for(int i = 0; i < options.length; i++) {
            Piece temp;
            Tint promotionColor = promotionService.getPromotionColor();
            switch(options[i]) {
                case QUEEN -> temp = new Queen(promotionColor, 0, 0);
                case ROOK -> temp = new Rook(promotionColor, 0, 0);
                case BISHOP -> temp = new Bishop(promotionColor, 0, 0);
                case KNIGHT -> temp = new Knight(promotionColor, 0, 0);
                default -> { continue; }
            }

            int x = startX + i * size;

            temp.setX(x);
            temp.setY(startY);

            if(i == hoverIndex) {
                temp.setScale(temp.getScale() + temp.getMORE_SCALE());
            } else {
                temp.setScale(temp.getDEFAULT_SCALE());
            }
            drawPiece(g2, temp);
        }
    }

    public void drawPiece(Graphics2D g2, Piece piece) {
        int square = Board.getSquare();
        int drawSize = (int) (square * piece.getScale());
        int offset = (square - drawSize) / 2;

        g2.drawImage(
                piece.getImage(),
                piece.getX() + offset,
                piece.getY() + offset,
                drawSize,
                drawSize,
                null
        );
    }

    public void handleMenuInput() {
        if(!mouse.isClicked()) {
            return;
        }
        int startY = getHEIGHT()/2 + MENU_START_Y;
        int spacing = MENU_SPACING;

        for(int i = 0; i < optionsMenu.length; i++) {
            int y = startY + i * spacing;
            boolean isHovered = getHitbox(y).contains(mouse.getX(),
                    mouse.getY());
            if(isHovered) {
                fx.play(3);
                switch(i) {
                    case 0 -> gameService.startNewGame();
                    case 1 -> System.exit(0);
                }
                mouse.setClicked(false);
                break;
            }
        }
    }

    public static Rectangle getHitbox(int y) {
        Rectangle hitbox = new Rectangle(
                getWIDTH()/2 - 100,
                y - 30,
                200,
                40
        );
        return hitbox;
    }
}
