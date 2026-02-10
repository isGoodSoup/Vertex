package org.chess.service;

import org.chess.entities.*;
import org.chess.enums.Tint;
import org.chess.enums.Type;
import org.chess.gui.Mouse;

public class PromotionService {
    private long promotionStartTime = -1;
    private final long PROMOTION_DELAY = 3000;
    private Tint promotionColor;
    private Piece promotingPawn;

    private static Mouse mouse;
    private final PieceService pieceService;

    public PromotionService(PieceService pieceService, Mouse mouse) {
        this.pieceService = pieceService;
        PromotionService.mouse = mouse;
    }

    public long getPromotionStartTime() {
        return promotionStartTime;
    }

    public long getPROMOTION_DELAY() {
        return PROMOTION_DELAY;
    }

    public Tint getPromotionColor() {
        return promotionColor;
    }

    public void setPromotionColor(Tint promotionColor) {
        this.promotionColor = promotionColor;
    }

    public Piece getPromotingPawn() {
        return promotingPawn;
    }

    public void setPromotingPawn(Piece promotingPawn) {
        this.promotingPawn = promotingPawn;
    }

    public boolean checkPromotion(Piece p) {
        if(p instanceof Pawn) {
            if((p.getColor() == Tint.WHITE && p.getRow() == 0) ||
                    (p.getColor() == Tint.BLACK && p.getRow() == 7)) {
                BooleanService.isPromotionPending = true;
                promotingPawn = p;
                return true;
            }
        }
        return false;
    }

    public void promotion() {
        if(!BooleanService.isPromotionPending) { return; }
        if (promotingPawn == null) { return; }

        int size = Board.getSquare();
        int totalWidth = size * 4;
        int startX = (GUIService.getWIDTH() - totalWidth) / 2;
        int startY = (GUIService.getHEIGHT() - size) / 2;

        Type[] options = {Type.QUEEN, Type.ROOK, Type.BISHOP, Type.KNIGHT};
        int selectedIndex = -1;

        if (promotionStartTime == -1) {
            promotionStartTime = System.currentTimeMillis();
        }

        for(int i = 0; i < options.length; i++) {
            int x0 = startX + i * size;
            int x1 = x0 + size;

            if(mouse.wasPressed() &&
                    mouse.getX() >= x0 && mouse.getX() <= x1 &&
                    mouse.getY() >= startY && mouse.getY() <= startY + size) {
                selectedIndex = i;
                break;
            }
        }

        if (selectedIndex == -1 && System.currentTimeMillis() - promotionStartTime >= PROMOTION_DELAY) {
            selectedIndex = 0;
        }

        if (selectedIndex == -1) {
            return;
        }

        pieceService.getPieces().remove(promotingPawn);

        Piece promotedPiece = switch(options[selectedIndex]) {
            case QUEEN -> new Queen(promotingPawn.getColor(),
                    promotingPawn.getCol(), promotingPawn.getRow());
            case ROOK -> new Rook(promotingPawn.getColor(),
                    promotingPawn.getCol(), promotingPawn.getRow());
            case BISHOP -> new Bishop(promotingPawn.getColor(),
                    promotingPawn.getCol(), promotingPawn.getRow());
            case KNIGHT -> new Knight(promotingPawn.getColor(),
                    promotingPawn.getCol(), promotingPawn.getRow());
            default -> throw new IllegalStateException("Unexpected promotion type");
        };

        pieceService.getPieces().add(promotedPiece);
        BoardService.getBoardState()[promotedPiece.getCol()][promotedPiece.getRow()] =
                promotedPiece;
        promotingPawn = null;
        BooleanService.isPromotionPending = false;
        promotionStartTime = -1;
        pieceService.switchTurns();
    }
}
