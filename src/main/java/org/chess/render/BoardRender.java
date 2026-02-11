package org.chess.render;

import org.chess.entities.Board;
import org.chess.entities.Piece;
import org.chess.service.*;

import java.awt.*;
import java.awt.image.BufferedImage;

public class BoardRender {
    private final GUIService guiService;
    private final PieceService pieceService;
    private final BoardService boardService;
    private final PromotionService promotionService;

    public BoardRender(GUIService guiService,
                       PieceService pieceService,
                       BoardService boardService,
                       PromotionService promotionService) {
        this.guiService = guiService;
        this.pieceService = pieceService;
        this.boardService = boardService;
        this.promotionService = promotionService;
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

        if(hoverX >= 0 && hoverY >= 0) {
            g2.setColor(ColorRender.getColor(new Color(200, 155, 100, 180),
                    false));
            g2.fillRect(GUIService.getEXTRA_WIDTH() + hoverX * Board.getSquare(),
                    hoverY * Board.getSquare(),
                    Board.getSquare(),
                    Board.getSquare());
        }

        Piece selectedPiece = pieceService.getMoveManager() != null
                ? pieceService.getMoveManager().getSelectedPiece() : null;

        for(Piece p : pieceService.getPieces()) {
            if(p != currentPiece) {
                BufferedImage img = (p == hoveredPiece) ? hoveredPiece.getHovered() : p.getImage();
                drawPiece(g2, p, ColorRender.getSprite(img, true));
            }
        }

        if(currentPiece != null) {
            if(!BooleanService.isDragging) {
                currentPiece.setScale(currentPiece.getDEFAULT_SCALE());
            }
            drawPiece(g2, currentPiece);
        }
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
                g2.fillRect(GUIService.getEXTRA_WIDTH() + (col * SQUARE),
                        row * SQUARE,
                        SQUARE,
                        SQUARE);
                g2.setFont(GUIService.getFont(14));
                g2.setColor(isEven ? ODD : EVEN);
                g2.drawString(BoardService.getSquareName(col, row),
                        GUIService.getEXTRA_WIDTH() + (col * SQUARE) + PADDING,
                        row * SQUARE + SQUARE - PADDING);
            }
        }
    }

    public void drawPiece(Graphics2D g2, Piece piece) {
        drawPiece(g2, piece, null);
    }

    public void drawPiece(Graphics2D g2, Piece piece, BufferedImage override) {
        int square = Board.getSquare();
        int drawSize = (int) (square * piece.getScale());
        int offset = (square - drawSize)/2;

        g2.drawImage(
                override != null ? override :
                        piece.getFilteredSprite(piece.getImage()),
                piece.getX() + offset + GUIService.getEXTRA_WIDTH(),
                piece.getY() + offset,
                drawSize,
                drawSize,
                null
        );
    }
}
