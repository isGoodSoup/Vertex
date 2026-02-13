package org.chess.render;

import org.chess.entities.Board;
import org.chess.entities.Piece;
import org.chess.gui.Colors;
import org.chess.input.Mouse;
import org.chess.service.*;

import java.awt.*;
import java.awt.image.BufferedImage;

public class BoardRender {
    private RenderContext render;
    private GUIService guiService;
    private PieceService pieceService;
    private BoardService boardService;
    private PromotionService promotionService;

    public BoardRender(RenderContext render) {
        this.render = render;
    }

    public void setRender(RenderContext render) {
        this.render = render;
    }

    public GUIService getGuiService() {
        return guiService;
    }

    public void setGuiService(GUIService guiService) {
        this.guiService = guiService;
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

    public int getBoardOriginX() {
        int leftPanelWidth = render.scale(RenderContext.BASE_WIDTH/2);
        int totalBoardWidth = Board.getSquare() * boardService.getBoard().getCOL();
        int scaledBoardWidth = render.scale(totalBoardWidth);
        int middlePanelWidth =
                render.scale(RenderContext.BASE_WIDTH - 2 * RenderContext.BASE_WIDTH/2);
        int centerOffset = (middlePanelWidth - scaledBoardWidth)/2;
        return render.getOffsetX() + leftPanelWidth + centerOffset;
    }

    public int getBoardOriginY() {
        int totalBoardHeight = Board.getSquare() * boardService.getBoard().getROW();
        int scaledBoardHeight = render.scale(totalBoardHeight);
        int panelHeight = render.scale(RenderContext.BASE_HEIGHT);
        int centerOffset = (panelHeight - scaledBoardHeight) / 2;
        return render.getOffsetY() + centerOffset;
    }

    public void drawBoard(Graphics2D g2) {
        Piece currentPiece = PieceService.getPiece();
        Piece hoveredPiece = pieceService.getHoveredPieceKeyboard();
        int hoverX = pieceService.getHoveredSquareX();
        int hoverY = pieceService.getHoveredSquareY();

        drawBaseBoard(g2);

        g2.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
        );

        if (hoverX >= 0 && hoverY >= 0) {
            int squareSize = render.scale(Board.getSquare());
            guiService.drawBox(g2, 4, getBoardOriginX() + hoverX * squareSize,
                    getBoardOriginY() + hoverY * squareSize, squareSize,
                    squareSize, 16, 16, true, false);
        }

        Piece selectedPiece = pieceService.getMoveManager() != null
                ? pieceService.getMoveManager().getSelectedPiece() : null;

        for (Piece p : pieceService.getPieces()) {
            if (p != currentPiece) {
                BufferedImage img = (p == hoveredPiece) ? hoveredPiece.getHovered() : p.getImage();
                drawPiece(g2, p, Colorblindness.filter(img));
            }
        }

        if (currentPiece != null) {
            currentPiece.setScale(currentPiece.getDEFAULT_SCALE());
            drawPiece(g2, currentPiece);
        }
    }

    public void drawBaseBoard(Graphics2D g2) {
        g2.setColor(Colorblindness.filter(Colors.getBACKGROUND()));
        g2.fillRect(0, 0, RenderContext.BASE_WIDTH, RenderContext.BASE_HEIGHT);
        final int ROW = boardService.getBoard().getROW();
        final int COL = boardService.getBoard().getCOL();
        final int SQUARE = render.scale(Board.getSquare());
        final int PADDING = render.scale(Board.getPadding());

        int square = render.scale(Board.getSquare());
        float edgePadding = 0.15f;
        int boardSize = square * boardService.getBoard().getCOL();
        int edgeSize = (int) (boardSize * (1 + edgePadding));

        int originX = getBoardOriginX() - (edgeSize - boardSize)/2;
        int originY = getBoardOriginY() - (edgeSize - boardSize)/2;

        g2.setColor(Colorblindness.filter(Colors.getEDGE()));
        g2.fillRect(originX, originY, edgeSize, edgeSize);

        for (int row = 0; row < ROW; row++) {
            for (int col = 0; col < COL; col++) {
                boolean isEven = (row + col) % 2 == 0;
                g2.setColor(isEven ? Colorblindness.filter(Colors.getBACKGROUND())
                        : Colorblindness.filter(Colors.getFOREGROUND()));
                g2.fillRect(
                        getBoardOriginX() + col * SQUARE,
                        getBoardOriginY() + row * SQUARE,
                        SQUARE,
                        SQUARE
                );

                g2.setFont(GUIService.getFont(16));
                g2.setColor(isEven ? Colorblindness.filter(Colors.getFOREGROUND())
                        : Colorblindness.filter(Colors.getBACKGROUND()));
                g2.drawString(
                        BoardService.getSquareName(col, row),
                        getBoardOriginX() + col * SQUARE + PADDING,
                        getBoardOriginY() + row * SQUARE + SQUARE - PADDING
                );
            }
        }
    }

    public void drawPiece(Graphics2D g2, Piece piece) {
        drawPiece(g2, piece, null);
    }

    public void drawPiece(Graphics2D g2, Piece piece, BufferedImage override) {
        int square = render.scale(Board.getSquare());
        int size = (int) (square * piece.getScale());
        Mouse mouse = guiService.getMouse();
        int offset = (square - size)/2;

        Rectangle hitbox =
                new Rectangle(getBoardOriginX() + render.scale(piece.getX())
                        + offset,getBoardOriginY() + render.scale(piece.getY()) +
                        offset, size, size);

        boolean isHovered = hitbox.contains(mouse.getX(), mouse.getY());
        if(isHovered) {
            override = piece.getHovered();
        }

        g2.drawImage(
                override != null ? override : piece.getImage(),
                getBoardOriginX() + piece.getX() + offset,
                getBoardOriginY() + piece.getY() + offset,
                size,
                size,
                null
        );
    }
}