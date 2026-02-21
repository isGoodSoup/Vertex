package org.lud.engine.render;

import org.lud.engine.entities.Board;
import org.lud.engine.entities.Button;
import org.lud.engine.entities.King;
import org.lud.engine.entities.Piece;
import org.lud.engine.enums.GameState;
import org.lud.engine.enums.Games;
import org.lud.engine.gui.Colors;
import org.lud.engine.input.Mouse;
import org.lud.engine.input.MouseInput;
import org.lud.engine.service.*;

import java.awt.*;
import java.awt.image.BufferedImage;

public class BoardRender {
    private Button backButton;
    private Button undoButton;
    private Button resetButton;
    private RenderContext render;
    private UIService uiService;
    private PieceService pieceService;
    private BoardService boardService;
    private PromotionService promotionService;
    private GameService gameService;
    private Mouse mouse;
    private MouseInput mouseInput;

    public BoardRender(RenderContext render) {
        this.render = render;
    }

    public void setRender(RenderContext render) {
        this.render = render;
    }

    public UIService getUIService() {
        return uiService;
    }

    public void setUIService(UIService UIService) {
        this.uiService = UIService;
    }

    public PieceService getPieceService() {
        return pieceService;
    }

    public void setPieceService(PieceService pieceService) {
        this.pieceService = pieceService;
    }

    public BoardService getBoardService() {
        return boardService;
    }

    public void setBoardService(BoardService boardService) {
        this.boardService = boardService;
    }

    public PromotionService getPromotionService() {
        return promotionService;
    }

    public void setPromotionService(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    public GameService getGameService() {
        return gameService;
    }

    public void setGameService(GameService gameService) {
        this.gameService = gameService;
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

    public void setMouseInput(MouseInput mouseInput) {
        this.mouseInput = mouseInput;
    }

    public int getBoardOriginX() {
        int leftPanelWidth = render.scale(RenderContext.BASE_WIDTH/2);
        int totalBoardWidth = Board.getSquare() * boardService.getBoard().getCol();
        int scaledBoardWidth = render.scale(totalBoardWidth);
        int middlePanelWidth =
                render.scale(RenderContext.BASE_WIDTH - 2 * RenderContext.BASE_WIDTH/2);
        int centerOffset = (middlePanelWidth - scaledBoardWidth)/2;
        return render.getOffsetX() + leftPanelWidth + centerOffset;
    }

    public int getBoardOriginY() {
        int totalBoardHeight = Board.getSquare() * boardService.getBoard().getRow();
        int scaledBoardHeight = render.scale(totalBoardHeight);
        int panelHeight = render.scale(RenderContext.BASE_HEIGHT);
        int centerOffset = (panelHeight - scaledBoardHeight) / 2;
        return render.getOffsetY() + centerOffset;
    }

    public void drawBoard(Graphics2D g2) {
        Piece currentPiece = PieceService.getHeldPiece();
        Piece hoveredPiece = pieceService.getHoveredPieceKeyboard();
        int hoverX = pieceService.getHoveredSquareX();
        int hoverY = pieceService.getHoveredSquareY();

        int buttonX = 32;
        int buttonY = RenderContext.BASE_HEIGHT - 100;
        int offset = render.scale(80);

        drawBaseBoard(g2);
        g2.drawImage(
                initButton(backButton, buttonX, buttonY,
                        render.getMenuRender().getPREVIOUS_PAGE(),
                        render.getMenuRender().getPREVIOUS_PAGE_ON(),
                        () -> gameService.setState(GameState.MENU)),
                buttonX, buttonY, null
        );
        buttonX += offset;

        g2.drawImage(
                initButton(undoButton, buttonX, buttonY,
                        render.getMenuRender().getUNDO(),
                        render.getMenuRender().getUNDO_HIGHLIGHTED(),
                        () -> render.getMovesManager().undoLastMove()),
                buttonX, buttonY, null
        );
        buttonX += offset;

        g2.drawImage(
                initButton(resetButton, buttonX, buttonY,
                        render.getMenuRender().getRESET(),
                        render.getMenuRender().getRESET_HIGHLIGHTED(),
                        () -> boardService.resetBoard()),
                buttonX, buttonY, null
        );

        g2.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
        );

        if(hoverX >= 0 && hoverY >= 0) {
            int squareSize = render.scale(Board.getSquare());

            UIService.drawBox(g2, 4, getBoardOriginX() + hoverX * squareSize,
                    getBoardOriginY() + hoverY * squareSize, squareSize,
                    squareSize, MenuRender.getARC()/4,
                    true, false, 180);
        }

        Piece selectedPiece = pieceService.getMoveManager() != null
                ? pieceService.getMoveManager().getSelectedPiece() : null;

        synchronized (pieceService.getPieces()) {
            for (Piece p : pieceService.getPieces()) {
                drawPiece(g2, p, Colorblindness.filter(pieceService.getSprite(p)));
            }
        }

        if(currentPiece != null) {
            currentPiece.setScale(currentPiece.getDEFAULT_SCALE());
            drawPiece(g2, currentPiece);
        }
    }

    public void drawBaseBoard(Graphics2D g2) {
        g2.setColor(Colorblindness.filter(Colors.getBackground()));
        g2.fillRect(0, 0, RenderContext.BASE_WIDTH, RenderContext.BASE_HEIGHT);

        final int ROW = boardService.getBoard().getRow();
        final int COL = boardService.getBoard().getCol();
        final int SQUARE = render.scale(Board.getSquare());
        final int PADDING = render.scale(Board.getPadding());

        float edgePadding = 0.15f;
        int boardSize = SQUARE * COL;
        int edgeSize = (int) (boardSize * (1 + edgePadding));
        int originX = getBoardOriginX() - (edgeSize - boardSize)/2;
        int originY = getBoardOriginY() - (edgeSize - boardSize)/2;

        g2.setColor(Colorblindness.filter(Colors.getEdge()));
        g2.fillRect(originX, originY, edgeSize, edgeSize);

        for(int row = 0; row < ROW; row++) {
            for(int col = 0; col < COL; col++) {
                boolean isEven = (row + col) % 2 == 0;

                g2.setColor(isEven ?
                        Colorblindness.filter(Colors.getBackground())
                        : Colorblindness.filter(Colors.getForeground()));
                g2.fillRect(
                        getBoardOriginX() + col * SQUARE,
                        getBoardOriginY() + row * SQUARE,
                        SQUARE,
                        SQUARE
                );

                g2.setFont(UIService.getFontBold(16));

                if(col == 0) {
                    String number = String.valueOf(ROW - row);
                    g2.setColor(isEven ? Colorblindness.filter(Colors.getForeground())
                            : Colorblindness.filter(Colors.getBackground()));
                    g2.drawString(
                            number,
                            getBoardOriginX() + col * SQUARE + PADDING,
                            getBoardOriginY() + row * SQUARE + SQUARE - PADDING
                    );
                }

                if(row == ROW - 1) {
                    char letter = (char) ('a' + col);
                    g2.setColor(isEven ? Colorblindness.filter(Colors.getForeground())
                            : Colorblindness.filter(Colors.getBackground()));

                    FontMetrics fm = g2.getFontMetrics();
                    int letterWidth = fm.stringWidth(String.valueOf(letter));
                    int drawX = getBoardOriginX() + col * SQUARE + SQUARE - PADDING - letterWidth;
                    int drawY = getBoardOriginY() + row * SQUARE + SQUARE - PADDING;
                    g2.drawString(String.valueOf(letter), drawX, drawY);
                }

            }
        }
    }

    public void drawPiece(Graphics2D g2, Piece piece) {
        drawPiece(g2, piece, null);
    }

    public void drawPiece(Graphics2D g2, Piece piece, BufferedImage override) {
        int square = render.scale(Board.getSquare());
        int size = (int) (square * piece.getScale());
        int offset = (square - size)/2;

        if(piece instanceof King && GameService.getGame() == Games.CHECKERS) {
            override = pieceService.getKingSprites(piece);
        }

        g2.drawImage(
                override != null ? override : pieceService.getSprite(piece),
                getBoardOriginX() + piece.getX() + offset,
                getBoardOriginY() + piece.getY() + offset,
                size,
                size,
                null
        );
    }

    private Button createButton(int x, int y, int w, int h, Runnable action) {
        Button b = new Button(x, y, w, h, action);
        render.getMenuRender().getButtons().put(b, new Rectangle(x, y, w, h));
        return b;
    }

    private BufferedImage initButton(Button button, int buttonX, int buttonY,
                                     BufferedImage baseImg, BufferedImage altImg, Runnable action) {
        if(button == null) {
            button = createButton(buttonX, buttonY, baseImg.getWidth(), baseImg.getHeight(), action);
        }
        return render.isHovered(button) ? altImg : baseImg;
    }
}